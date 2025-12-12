#!/usr/bin/env bash

# Fix Line Endings for Shell Scripts
# Converts CRLF (Windows) to LF (Unix) line endings
# Usage: ./fix-line-endings.sh

echo "Fixing line endings for all shell scripts..."

# Find and fix all .sh files
find . -name "*.sh" -type f -print0 | while IFS= read -r -d '' file; do
    if file "$file" | grep -q "CRLF"; then
        echo "  Fixing: $file"
        sed -i 's/\r$//' "$file"
    fi
done

echo ""
echo "✓ Line endings fixed!"
echo ""
echo "Testing scripts..."

# Test the main redeploy scripts
for script in usuario/infrastructure/redeploy.sh inventario/infrastructure/redeploy.sh carrito/infrastructure/redeploy.sh redeploy-all-services.sh; do
    if [ -f "$script" ]; then
        if bash -n "$script" 2>/dev/null; then
            echo "  ✓ $script - syntax OK"
        else
            echo "  ✗ $script - syntax error"
        fi
    fi
done

echo ""
echo "Done!"
