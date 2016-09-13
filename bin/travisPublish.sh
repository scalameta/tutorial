openssl aes-256-cbc -K $encrypted_3f3a855bcd06_key -iv $encrypted_3f3a855bcd06_iv
  -in deploy_key.enc -out deploy_key -d
eval `ssh-agent -s`
ssh-add deploy_key
git config --global user.email "you@example.com"
git config --global user.name "Your Name"
