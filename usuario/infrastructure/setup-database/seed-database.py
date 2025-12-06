#!/usr/bin/env python3

"""
DynamoDB Seed Script - Python version

Populates the lunari-users DynamoDB table with sample e-commerce client data.

Usage:
    python3 seed-database.py [environment]

Examples:
    python3 seed-database.py dev
    python3 seed-database.py prod

Requirements:
    pip install boto3
"""

import boto3
import json
import sys
import os
from pathlib import Path

# Configuration
REGION = os.environ.get('AWS_REGION', 'us-east-1')
ENV = sys.argv[1] if len(sys.argv) > 1 else 'dev'
TABLE_NAME = f'lunari-users-{ENV}'

# Colors for console output
class Colors:
    RESET = '\033[0m'
    GREEN = '\033[32m'
    YELLOW = '\033[1;33m'
    RED = '\033[31m'
    CYAN = '\033[36m'
    GRAY = '\033[90m'

def log(message, color='RESET'):
    """Print colored log message"""
    color_code = getattr(Colors, color.upper(), Colors.RESET)
    print(f"{color_code}{message}{Colors.RESET}")

def check_table_exists(dynamodb):
    """Check if the DynamoDB table exists"""
    try:
        dynamodb.Table(TABLE_NAME).load()
        return True
    except dynamodb.meta.client.exceptions.ResourceNotFoundException:
        return False
    except Exception as e:
        raise e

def seed_database():
    """Main function to seed the database"""
    log('\n========================================', 'CYAN')
    log('  DynamoDB Seed Script - LUNARi Users', 'CYAN')
    log('========================================\n', 'CYAN')

    log(f'Environment: {ENV}', 'GRAY')
    log(f'Table Name: {TABLE_NAME}', 'GRAY')
    log(f'Region: {REGION}\n', 'GRAY')

    # Initialize DynamoDB client
    try:
        dynamodb = boto3.resource('dynamodb', region_name=REGION)
    except Exception as e:
        log(f'ERROR: Failed to initialize AWS SDK: {e}', 'RED')
        log('\nMake sure you have AWS credentials configured:', 'YELLOW')
        log('  aws configure', 'GRAY')
        sys.exit(1)

    # Check if table exists
    log('Checking if table exists...', 'YELLOW')
    if not check_table_exists(dynamodb):
        log(f"ERROR: Table '{TABLE_NAME}' does not exist!", 'RED')
        log('\nPlease create the table first:', 'YELLOW')
        log(f'  cd infrastructure', 'GRAY')
        log(f'  ./deploy-dynamodb.sh {ENV}\n', 'GRAY')
        sys.exit(1)
    log('✓ Table exists\n', 'GREEN')

    # Load seed data
    log('Loading seed data...', 'YELLOW')
    script_dir = Path(__file__).parent
    seed_file = script_dir / 'seed-data.json'

    if not seed_file.exists():
        log(f'ERROR: Seed data file not found: {seed_file}', 'RED')
        sys.exit(1)

    with open(seed_file, 'r', encoding='utf-8') as f:
        seed_data = json.load(f)

    log(f'✓ Loaded {len(seed_data)} users from seed-data.json\n', 'GREEN')

    # Get table
    table = dynamodb.Table(TABLE_NAME)

    # Insert users
    log('Inserting users into DynamoDB...\n', 'YELLOW')

    success_count = 0
    error_count = 0

    for user in seed_data:
        try:
            table.put_item(Item=user)
            log(f"  ✓ Inserted user: {user['username']} ({user['email']})", 'GREEN')
            success_count += 1
        except Exception as e:
            log(f"  ✗ Failed to insert {user['username']}: {str(e)}", 'RED')
            error_count += 1

    # Summary
    log('\n========================================', 'CYAN')
    log('  Summary', 'CYAN')
    log('========================================', 'CYAN')
    log(f'Total users: {len(seed_data)}', 'GRAY')
    log(f'Successfully inserted: {success_count}', 'GREEN')
    log(f'Failed: {error_count}', 'RED' if error_count > 0 else 'GRAY')
    log('========================================\n', 'CYAN')

    if success_count > 0:
        log('✓ Database seeded successfully!\n', 'GREEN')
        log('You can now query the data:', 'GRAY')
        log(f'  aws dynamodb scan --table-name {TABLE_NAME} --region {REGION}\n', 'GRAY')
        log('Or via the API:', 'GRAY')
        log('  curl http://your-api:8080/api/v1/users\n', 'GRAY')

if __name__ == '__main__':
    try:
        seed_database()
        log('Done!', 'GREEN')
        sys.exit(0)
    except KeyboardInterrupt:
        log('\n\nAborted by user', 'YELLOW')
        sys.exit(1)
    except Exception as e:
        log(f'\nERROR: {str(e)}', 'RED')
        import traceback
        traceback.print_exc()
        sys.exit(1)
