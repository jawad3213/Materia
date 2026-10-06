/**
 * Materia Full-Flow Demo Seeder
 *
 * Fills the database with a year of realistic procurement activity, so every page and the dashboard have data:
 * users, categories, suppliers, materials and stock history, then requisitions -> purchase orders -> goods
 * receipts (with quality rejections) -> returns to vendor -> invoices and credit notes -> payments, in every
 * status, dated over the last 12 months.
 *
 * Unlike the other scripts in this folder, it does not call the REST API: the API stamps every action with
 * today's date, so the dashboard's monthly trend, invoice aging and delivery punctuality would stay empty.
 * Instead it starts the backend with the built-in seeder enabled
 * (backend/src/main/java/com/materia/backend/infrastructure/seed/DemoDataSeeder.java). The seeder runs once:
 * it is skipped when the demo data is already there.
 *
 * Stop any backend already running on port 8080 first. The backend keeps running after seeding; stop it with
 * Ctrl+C and start it normally afterwards.
 *
 * Demo accounts (password Materia@2026):
 *   admins      y.benali@materia.ma, s.idrissi@materia.ma
 *   purchasers  k.elamrani@materia.ma, n.tazi@materia.ma, o.chraibi@materia.ma
 *   receivers   h.ouazzani@materia.ma, i.berrada@materia.ma, r.alaoui@materia.ma
 *
 * Usage: node scripts/seed-demo.mjs
 */

import { spawn } from 'node:child_process';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const backendDir = join(dirname(fileURLToPath(import.meta.url)), '..', 'backend');
const isWindows = process.platform === 'win32';
const mvnw = isWindows ? 'mvnw.cmd' : './mvnw';

console.log('Starting the backend with the demo seeder enabled...');

const backend = spawn(mvnw, ['spring-boot:run', '-Dspring-boot.run.arguments=--app.seed.enabled=true'], {
  cwd: backendDir,
  shell: isWindows,
  stdio: ['inherit', 'pipe', 'inherit'],
});

backend.stdout.on('data', (chunk) => {
  const text = chunk.toString();
  process.stdout.write(text);
  if (text.includes('Demo data seeded')) {
    console.log('\nDemo data seeded. Log in with y.benali@materia.ma / Materia@2026. Press Ctrl+C to stop the backend.');
  } else if (text.includes('Demo data already present')) {
    console.log('\nDemo data was already present, nothing to do. Press Ctrl+C to stop the backend.');
  } else if (text.includes('Application run failed')) {
    console.error('\nSeeding failed, see the error above. Nothing was saved (the seeder runs in one transaction).');
  }
});

backend.on('exit', (code) => process.exit(code ?? 0));
process.on('SIGINT', () => backend.kill('SIGINT'));
