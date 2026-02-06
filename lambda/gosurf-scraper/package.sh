#!/bin/bash
# Package Lambda function for deployment

set -e

echo "🔨 Packaging GoSurf Scraper Lambda..."

# Clean previous builds
rm -rf package
rm -f gosurf-scraper.zip

# Create package directory
mkdir -p package

# Install dependencies
echo "📦 Installing dependencies..."
pip install -r requirements.txt -t package/ --platform manylinux2014_x86_64 --only-binary=:all:

# Copy Lambda function
echo "📋 Copying Lambda function..."
cp lambda_function.py package/

# Create zip file
echo "🗜️  Creating deployment package..."
cd package
zip -r ../gosurf-scraper.zip . -q
cd ..

# Show package size
PACKAGE_SIZE=$(du -h gosurf-scraper.zip | cut -f1)
echo "✅ Package created: gosurf-scraper.zip ($PACKAGE_SIZE)"

# Clean up
rm -rf package

echo "🚀 Ready to deploy!"
echo ""
echo "Deploy with AWS CLI:"
echo "  aws lambda update-function-code --function-name gosurf-scraper --zip-file fileb://gosurf-scraper.zip"
