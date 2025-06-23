-- Clean up test data in proper order to avoid foreign key constraint violations
DELETE FROM ticket_history;
DELETE FROM feedback_entries;
DELETE FROM appointments;
DELETE FROM tickets;
DELETE FROM technician_skills;
DELETE FROM technicians;
DELETE FROM clients;

-- Reset identity sequences
ALTER TABLE ticket_history ALTER COLUMN id RESTART WITH 1;
ALTER TABLE feedback_entries ALTER COLUMN id RESTART WITH 1;
ALTER TABLE appointments ALTER COLUMN id RESTART WITH 1;
ALTER TABLE tickets ALTER COLUMN id RESTART WITH 1;
ALTER TABLE technician_skills ALTER COLUMN id RESTART WITH 1;
ALTER TABLE technicians ALTER COLUMN id RESTART WITH 1;
ALTER TABLE clients ALTER COLUMN id RESTART WITH 1; 