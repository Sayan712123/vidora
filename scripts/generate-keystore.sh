#!/usr/bin/env bash
#
# Creates the release keystore for Vidora and prints the four values you need to
# put in local.properties (or in the GitHub Actions secrets).
#
# Usage:
#   ./scripts/generate-keystore.sh
#
# The keystore file is written to the project root as `release.keystore` and is
# already listed in .gitignore - never commit it.

set -euo pipefail

KEYSTORE_FILE="${KEYSTORE_FILE:-release.keystore}"
ALIAS="${KEY_ALIAS:-vidora}"
VALIDITY_DAYS="${VALIDITY_DAYS:-10000}"

if [ -f "$KEYSTORE_FILE" ]; then
  echo "!! $KEYSTORE_FILE already exists. Move it aside first if you want a new one."
  exit 1
fi

if ! command -v keytool >/dev/null 2>&1; then
  echo "!! keytool was not found. Install a JDK 17 (it ships with keytool) and try again."
  exit 1
fi

read -r -s -p "Choose a keystore password: " STORE_PASSWORD
echo
read -r -s -p "Repeat the password: " STORE_PASSWORD_2
echo
if [ "$STORE_PASSWORD" != "$STORE_PASSWORD_2" ]; then
  echo "!! The passwords do not match."
  exit 1
fi

keytool -genkeypair \
  -v \
  -keystore "$KEYSTORE_FILE" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity "$VALIDITY_DAYS" \
  -storepass "$STORE_PASSWORD" \
  -keypass "$STORE_PASSWORD" \
  -dname "CN=Vidora, OU=Mobile, O=Vidora, L=, ST=, C=IN"

echo
echo "Keystore created: $KEYSTORE_FILE"
echo
echo "Add these lines to local.properties:"
echo
echo "RELEASE_KEYSTORE_PATH=$KEYSTORE_FILE"
echo "RELEASE_KEYSTORE_PASSWORD=<the password you just chose>"
echo "RELEASE_KEY_ALIAS=$ALIAS"
echo "RELEASE_KEY_PASSWORD=<the same password>"
echo
echo "For GitHub Actions, base64-encode the keystore and store it as a secret:"
echo
echo "  base64 -w0 $KEYSTORE_FILE    # -> secret KEYSTORE_BASE64"
echo "  KEYSTORE_PASSWORD            # -> secret KEYSTORE_PASSWORD"
echo "  KEY_ALIAS                    # -> secret KEY_ALIAS  ($ALIAS)"
echo "  KEY_PASSWORD                 # -> secret KEY_PASSWORD"
echo
echo "Keep a backup of $KEYSTORE_FILE and its password somewhere safe:"
echo "losing it means you can never publish an update to the same app listing."
