const fs = require('fs');
const p = 'c:/Users/elhai/OneDrive/Desktop/materia/frontend/src/shared/icons/index.ts';
let c = fs.readFileSync(p, 'utf8');
c = c.replace(/import\s+\{\s*ReactComponent\s+as\s+([a-zA-Z0-9_]+)\s*\}\s+from/g, 'import $1 from');
fs.writeFileSync(p, c);
console.log('Fixed index.ts successfully!');
