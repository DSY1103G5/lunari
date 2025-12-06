#!/usr/bin/env node

/**
 * DynamoDB Seed Script
 *
 * Populates the lunari-users DynamoDB table with sample e-commerce client data.
 *
 * Usage:
 *   node seed-database.js [environment]
 *
 * Examples:
 *   node seed-database.js dev
 *   node seed-database.js prod
 */

const { DynamoDBClient } = require("@aws-sdk/client-dynamodb");
const { DynamoDBDocumentClient, PutCommand, ScanCommand } = require("@aws-sdk/lib-dynamodb");
const fs = require('fs');
const path = require('path');

// Configuration
const REGION = process.env.AWS_REGION || 'us-east-1';
const ENV = process.argv[2] || 'dev';
const TABLE_NAME = `lunari-users-${ENV}`;

// Initialize DynamoDB Client
const client = new DynamoDBClient({ region: REGION });
const docClient = DynamoDBDocumentClient.from(client);

// Colors for console output
const colors = {
  reset: '\x1b[0m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  red: '\x1b[31m',
  cyan: '\x1b[36m',
  gray: '\x1b[90m'
};

function log(message, color = 'reset') {
  console.log(`${colors[color]}${message}${colors.reset}`);
}

async function checkTableExists() {
  try {
    const command = new ScanCommand({
      TableName: TABLE_NAME,
      Limit: 1
    });
    await docClient.send(command);
    return true;
  } catch (error) {
    if (error.name === 'ResourceNotFoundException') {
      return false;
    }
    throw error;
  }
}

async function seedDatabase() {
  log('\n========================================', 'cyan');
  log('  DynamoDB Seed Script - LUNARi Users', 'cyan');
  log('========================================\n', 'cyan');

  log(`Environment: ${ENV}`, 'gray');
  log(`Table Name: ${TABLE_NAME}`, 'gray');
  log(`Region: ${REGION}\n`, 'gray');

  // Check if table exists
  log('Checking if table exists...', 'yellow');
  const tableExists = await checkTableExists();

  if (!tableExists) {
    log(`ERROR: Table '${TABLE_NAME}' does not exist!`, 'red');
    log('\nPlease create the table first:', 'yellow');
    log(`  cd infrastructure`, 'gray');
    log(`  ./deploy-dynamodb.sh ${ENV}\n`, 'gray');
    process.exit(1);
  }
  log('✓ Table exists\n', 'green');

  // Load seed data
  log('Loading seed data...', 'yellow');
  const seedDataPath = path.join(__dirname, 'seed-data.json');
  const seedData = JSON.parse(fs.readFileSync(seedDataPath, 'utf8'));
  log(`✓ Loaded ${seedData.length} users from seed-data.json\n`, 'green');

  // Insert users
  log('Inserting users into DynamoDB...\n', 'yellow');

  let successCount = 0;
  let errorCount = 0;

  for (const user of seedData) {
    try {
      const command = new PutCommand({
        TableName: TABLE_NAME,
        Item: user
      });

      await docClient.send(command);
      log(`  ✓ Inserted user: ${user.username} (${user.email})`, 'green');
      successCount++;
    } catch (error) {
      log(`  ✗ Failed to insert ${user.username}: ${error.message}`, 'red');
      errorCount++;
    }
  }

  // Summary
  log('\n========================================', 'cyan');
  log('  Summary', 'cyan');
  log('========================================', 'cyan');
  log(`Total users: ${seedData.length}`, 'gray');
  log(`Successfully inserted: ${successCount}`, 'green');
  log(`Failed: ${errorCount}`, errorCount > 0 ? 'red' : 'gray');
  log('========================================\n', 'cyan');

  if (successCount > 0) {
    log('✓ Database seeded successfully!\n', 'green');
    log('You can now query the data:', 'gray');
    log(`  aws dynamodb scan --table-name ${TABLE_NAME} --region ${REGION}\n`, 'gray');
    log('Or via the API:', 'gray');
    log(`  curl http://your-api:8080/api/v1/users\n`, 'gray');
  }
}

// Run the script
seedDatabase()
  .then(() => {
    log('Done!', 'green');
    process.exit(0);
  })
  .catch((error) => {
    log(`\nERROR: ${error.message}`, 'red');
    console.error(error);
    process.exit(1);
  });
