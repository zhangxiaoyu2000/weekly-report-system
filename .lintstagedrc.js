module.exports = {
  // Frontend JavaScript/Vue files
  'frontend/**/*.{js,vue,ts}': [
    'cd frontend && npm run lint:fix',
    'cd frontend && npm run format',
  ],
  
  // Frontend JSON/YAML files
  'frontend/**/*.{json,yml,yaml}': [
    'prettier --write',
  ],
  
  // Backend Java files
  'backend/**/*.java': [
    'cd backend && mvn spotless:apply',
    'cd backend && mvn checkstyle:check',
  ],
  
  // Root level config files
  '*.{js,json,yml,yaml,md}': [
    'prettier --write',
  ],
  
  // Documentation files
  'docs/**/*.md': [
    'prettier --write',
  ],
  
  // Docker and deployment files
  '{deploy,docker}/**/*.{yml,yaml}': [
    'prettier --write',
  ],
};