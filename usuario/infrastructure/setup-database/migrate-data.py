#!/usr/bin/env python3

"""
Data Migration Script: PostgreSQL to DynamoDB
Migrates user data from PostgreSQL (JPA) to DynamoDB

Requirements:
    pip install psycopg2-binary boto3 python-dotenv
"""

import os
import sys
import json
import logging
from datetime import datetime, timezone
from decimal import Decimal
import psycopg2
import boto3
from dotenv import load_dotenv

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Load environment variables
load_dotenv()

# Role mapping (old roleId -> new RoleType)
ROLE_MAPPING = {
    1: {"roleId": 1, "roleName": "ADMIN", "roleDescription": "Administrator with full system access"},
    2: {"roleId": 2, "roleName": "PRODUCT_OWNER", "roleDescription": "Product owner with management capabilities"},
    3: {"roleId": 3, "roleName": "CLIENT", "roleDescription": "Regular client user"},
    4: {"roleId": 4, "roleName": "DEVOPS", "roleDescription": "DevOps engineer with infrastructure access"}
}

DEFAULT_ROLE = {"roleId": 3, "roleName": "CLIENT", "roleDescription": "Regular client user"}


def convert_datetime_to_iso(dt):
    """Convert datetime to ISO 8601 string"""
    if dt is None:
        return None
    if isinstance(dt, str):
        return dt
    return dt.isoformat()


def get_postgres_connection():
    """Create PostgreSQL connection"""
    try:
        conn = psycopg2.connect(
            host=os.getenv('POSTGRES_HOST', 'localhost'),
            port=os.getenv('POSTGRES_PORT', '5432'),
            database=os.getenv('POSTGRES_DB', 'lunari_users'),
            user=os.getenv('POSTGRES_USER', 'postgres'),
            password=os.getenv('POSTGRES_PASSWORD', 'postgres')
        )
        logger.info("✓ Connected to PostgreSQL")
        return conn
    except Exception as e:
        logger.error(f"✗ Failed to connect to PostgreSQL: {e}")
        sys.exit(1)


def get_dynamodb_resource():
    """Create DynamoDB resource"""
    try:
        # Check if using LocalStack
        endpoint_url = os.getenv('DYNAMODB_ENDPOINT')

        if endpoint_url:
            logger.info(f"Using LocalStack/DynamoDB Local: {endpoint_url}")
            dynamodb = boto3.resource(
                'dynamodb',
                endpoint_url=endpoint_url,
                region_name=os.getenv('AWS_REGION', 'us-east-1'),
                aws_access_key_id='fakeAccessKey',
                aws_secret_access_key='fakeSecretKey'
            )
        else:
            logger.info("Using AWS DynamoDB")
            dynamodb = boto3.resource(
                'dynamodb',
                region_name=os.getenv('AWS_REGION', 'us-east-1')
            )

        logger.info("✓ Connected to DynamoDB")
        return dynamodb
    except Exception as e:
        logger.error(f"✗ Failed to connect to DynamoDB: {e}")
        sys.exit(1)


def migrate_user(user_row, role_cache):
    """Transform PostgreSQL user row to DynamoDB item"""
    user_id, first_name, last_name, email, phone, profile_image, password, \
    role_id, is_active, last_login, is_verified, token_verification, \
    token_expiration, created_at, updated_at = user_row[:15]

    # Get role from cache or use default
    role = role_cache.get(role_id, DEFAULT_ROLE)

    # Prepare DynamoDB item
    item = {
        'userId': str(user_id),
        'firstName': first_name or '',
        'lastName': last_name or '',
        'email': email,
        'phone': phone or '',
        'profileImg': profile_image or '',
        'password': password or '',

        # Role (embedded)
        'role': role,
        'roleName': role['roleName'],  # For GSI

        # Status
        'isActive': bool(is_active),
        'isVerified': bool(is_verified),

        # Composite sort key for RoleActiveIndex
        'isActiveUserId': f"{is_active}#{user_id}",

        # Timestamps
        'createdAt': convert_datetime_to_iso(created_at),
        'updatedAt': convert_datetime_to_iso(updated_at),
        'memberSince': convert_datetime_to_iso(created_at),
    }

    # Optional fields
    if last_login:
        item['lastLogin'] = convert_datetime_to_iso(last_login)

    if token_verification:
        item['tokenVerification'] = token_verification

    if token_expiration:
        item['tokenExpiration'] = convert_datetime_to_iso(token_expiration)

    # Initialize new fields with defaults
    item['level'] = 1
    item['points'] = 0

    item['purchases'] = {
        'totalCount': 0,
        'totalSpent': '0.00'
    }

    item['reviews'] = {
        'totalCount': 0
    }

    item['favorites'] = {
        'serviceIds': [],
        'count': 0
    }

    item['preferences'] = {
        'language': 'es',
        'currency': 'CLP',
        'notifications': {
            'email': True,
            'sms': False,
            'push': True
        }
    }

    item['metadata'] = {}

    return item


def migrate_data(dry_run=False):
    """Main migration function"""
    logger.info("=" * 60)
    logger.info("Starting Data Migration: PostgreSQL → DynamoDB")
    logger.info("=" * 60)

    if dry_run:
        logger.warning("DRY RUN MODE - No data will be written to DynamoDB")

    # Connect to databases
    pg_conn = get_postgres_connection()
    dynamodb = get_dynamodb_resource()

    table_name = os.getenv('DYNAMODB_TABLE_NAME', 'lunari-users-dev')
    table = dynamodb.Table(table_name)
    logger.info(f"Target table: {table_name}")

    # Build role cache
    logger.info("Building role cache...")
    role_cache = {}
    try:
        with pg_conn.cursor() as cursor:
            cursor.execute("SELECT id, name, description FROM user_role")
            for role_row in cursor.fetchall():
                role_id, role_name, role_desc = role_row
                # Map to new role structure
                if role_id in ROLE_MAPPING:
                    role_cache[role_id] = ROLE_MAPPING[role_id]
                else:
                    role_cache[role_id] = {
                        "roleId": role_id,
                        "roleName": role_name.upper(),
                        "roleDescription": role_desc or ""
                    }
        logger.info(f"✓ Loaded {len(role_cache)} roles")
    except Exception as e:
        logger.warning(f"Could not load roles from database: {e}")
        logger.info("Using default role mapping")
        role_cache = ROLE_MAPPING

    # Migrate users
    logger.info("\nMigrating users...")
    migrated_count = 0
    error_count = 0

    try:
        with pg_conn.cursor() as cursor:
            # Adjust column selection based on your actual schema
            cursor.execute("""
                SELECT
                    id, first_name, last_name, email, phone, profile_image,
                    password, role_id, is_active, last_login, is_verified,
                    token_verification, token_expiration, created_at, updated_at
                FROM users
                ORDER BY created_at
            """)

            total_users = cursor.rowcount
            logger.info(f"Found {total_users} users to migrate")

            for user_row in cursor:
                try:
                    item = migrate_user(user_row, role_cache)

                    if not dry_run:
                        table.put_item(Item=item)

                    migrated_count += 1

                    if migrated_count % 100 == 0:
                        logger.info(f"Migrated {migrated_count} users...")

                except Exception as e:
                    error_count += 1
                    logger.error(f"Error migrating user {user_row[0]}: {e}")

    except Exception as e:
        logger.error(f"Error during migration: {e}")
        sys.exit(1)

    finally:
        pg_conn.close()

    # Summary
    logger.info("\n" + "=" * 60)
    logger.info("Migration Summary")
    logger.info("=" * 60)
    logger.info(f"Total users processed: {migrated_count + error_count}")
    logger.info(f"Successfully migrated: {migrated_count}")
    logger.info(f"Errors: {error_count}")

    if dry_run:
        logger.warning("DRY RUN - No data was written to DynamoDB")
    else:
        logger.info("✓ Migration completed successfully!")

    return migrated_count, error_count


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='Migrate users from PostgreSQL to DynamoDB')
    parser.add_argument('--dry-run', action='store_true', help='Run without writing to DynamoDB')
    args = parser.parse_args()

    try:
        migrate_data(dry_run=args.dry_run)
    except KeyboardInterrupt:
        logger.warning("\nMigration interrupted by user")
        sys.exit(1)
