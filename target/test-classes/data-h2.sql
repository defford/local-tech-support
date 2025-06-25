-- Sample Data for Tech Support Server
-- H2-compatible version for testing
-- This script populates the database with realistic sample data for development and testing

-- ===============================
-- CLIENTS
-- ===============================
INSERT INTO clients (first_name, last_name, email, phone, address, status, notes, created_at, updated_at) VALUES
('John', 'Doe', 'john.doe@example.com', '555-0101', '123 Main St, Anytown, USA', 'ACTIVE', 'Regular customer, prefers email contact', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sarah', 'Johnson', 'sarah.johnson@company.com', '555-0102', '456 Oak Ave, Business District', 'ACTIVE', 'IT Manager at local company', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Mike', 'Chen', 'mike.chen@startup.io', '555-0103', '789 Tech Blvd, Innovation Center', 'ACTIVE', 'Startup founder, tech-savvy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Emily', 'Davis', 'emily.davis@school.edu', '555-0104', '321 University Dr, Campus Area', 'ACTIVE', 'Professor, needs help with research equipment', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Robert', 'Wilson', 'bob.wilson@retired.com', '555-0105', '654 Quiet Lane, Suburbs', 'ACTIVE', 'Retired, needs patient explanations', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Lisa', 'Martinez', 'lisa.martinez@design.com', '555-0106', '987 Creative St, Arts District', 'ACTIVE', 'Graphic designer, Mac user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('David', 'Brown', 'david.brown@legal.com', '555-0107', '147 Justice Ave, Downtown', 'SUSPENDED', 'Payment issues - suspended account', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Jennifer', 'Taylor', 'jen.taylor@healthcare.org', '555-0108', '258 Medical Center Dr', 'ACTIVE', 'Hospital administrator', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===============================
-- TECHNICIANS
-- ===============================
INSERT INTO technicians (full_name, email, status) VALUES
('Alex Rodriguez', 'alex.rodriguez@techsupport.com', 'ACTIVE'),
('Jamie Kim', 'jamie.kim@techsupport.com', 'ACTIVE'),
('Morgan Foster', 'morgan.foster@techsupport.com', 'ACTIVE'),
('Casey Thompson', 'casey.thompson@techsupport.com', 'ON_VACATION'),
('Jordan Lee', 'jordan.lee@techsupport.com', 'ACTIVE');

-- ===============================
-- TECHNICIAN SKILLS
-- ===============================
INSERT INTO technician_skills (technician_id, service_type) VALUES
-- Alex Rodriguez - Hardware specialist
(1, 'HARDWARE'),
-- Jamie Kim - Software specialist  
(2, 'SOFTWARE'),
-- Morgan Foster - Full stack (both hardware and software)
(3, 'HARDWARE'),
(3, 'SOFTWARE'),
-- Casey Thompson - Hardware specialist (currently on vacation)
(4, 'HARDWARE'),
-- Jordan Lee - Software specialist
(5, 'SOFTWARE');

-- ===============================
-- TICKETS
-- ===============================
INSERT INTO tickets (client_id, technician_id, service_type, description, status, created_at, due_at) VALUES
-- Open tickets (various states and urgency)
(1, 1, 'HARDWARE', 'Desktop computer won''t boot - black screen on startup. Heard clicking sounds from hard drive.', 'OPEN', DATEADD('HOUR', -2, CURRENT_TIMESTAMP), DATEADD('HOUR', 22, CURRENT_TIMESTAMP)),
(2, 2, 'SOFTWARE', 'Email client keeps crashing when trying to send attachments larger than 5MB. Using Outlook 2021.', 'OPEN', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), DATEADD('HOUR', 47, CURRENT_TIMESTAMP)),
(3, 3, 'HARDWARE', 'Laptop overheating and shutting down randomly. Fan seems to be running constantly.', 'OPEN', DATEADD('MINUTE', -30, CURRENT_TIMESTAMP), DATEADD('MINUTE', 1410, CURRENT_TIMESTAMP)),
(4, NULL, 'SOFTWARE', 'Database connection issues - application throws timeout errors during peak hours.', 'OPEN', DATEADD('MINUTE', -15, CURRENT_TIMESTAMP), DATEADD('MINUTE', 2865, CURRENT_TIMESTAMP)),
(5, NULL, 'HARDWARE', 'Printer not responding to print jobs. Status shows offline but device is powered on.', 'OPEN', DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), DATEADD('MINUTE', 1430, CURRENT_TIMESTAMP)),

-- Overdue tickets (past due date)
(6, 3, 'SOFTWARE', 'Adobe Creative Suite licensing error - can''t open Photoshop or Illustrator.', 'OPEN', DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(8, 1, 'HARDWARE', 'Network connectivity issues - intermittent connection drops every few minutes.', 'OPEN', DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('HOUR', -2, CURRENT_TIMESTAMP)),

-- Recently closed tickets
(1, 1, 'HARDWARE', 'Monitor display flickering - replaced faulty VGA cable.', 'CLOSED', DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('HOUR', 23, CURRENT_TIMESTAMP)),
(2, 2, 'SOFTWARE', 'Windows update stuck at 35% - resolved by running update troubleshooter.', 'CLOSED', DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('HOUR', 46, CURRENT_TIMESTAMP)),
(3, 3, 'HARDWARE', 'Keyboard keys sticking - cleaned and replaced membrane.', 'CLOSED', DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('HOUR', 21, CURRENT_TIMESTAMP)),
(4, 2, 'SOFTWARE', 'Browser redirecting to suspicious websites - removed malware and updated security.', 'CLOSED', DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('HOUR', 44, CURRENT_TIMESTAMP)),
(5, 1, 'HARDWARE', 'External hard drive not recognized - updated USB drivers.', 'CLOSED', DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('HOUR', 19, CURRENT_TIMESTAMP)),
(6, 3, 'SOFTWARE', 'Microsoft Office activation issues - reactivated with valid license key.', 'CLOSED', DATEADD('DAY', -6, CURRENT_TIMESTAMP), DATEADD('HOUR', 42, CURRENT_TIMESTAMP)),
(7, NULL, 'HARDWARE', 'Webcam not working for video calls - driver compatibility issue.', 'CLOSED', DATEADD('DAY', -7, CURRENT_TIMESTAMP), DATEADD('HOUR', 17, CURRENT_TIMESTAMP)),
(8, 2, 'SOFTWARE', 'Slow computer performance - cleaned startup programs and ran disk cleanup.', 'CLOSED', DATEADD('DAY', -8, CURRENT_TIMESTAMP), DATEADD('HOUR', 40, CURRENT_TIMESTAMP));

-- ===============================
-- TICKET HISTORY (Using ordinal values: OPEN=0, CLOSED=1)
-- ===============================
INSERT INTO ticket_history (ticket_id, status, description, created_by, created_at, updated_at) VALUES
-- History for open tickets
(1, 0, 'Ticket created - Desktop boot failure reported', 'system', DATEADD('HOUR', -2, CURRENT_TIMESTAMP), DATEADD('HOUR', -2, CURRENT_TIMESTAMP)),
(1, 0, 'Assigned to technician: Alex Rodriguez', 'admin', DATEADD('MINUTE', -90, CURRENT_TIMESTAMP), DATEADD('MINUTE', -90, CURRENT_TIMESTAMP)),
(1, 0, 'Initial diagnosis: Potential hard drive failure based on clicking sounds', 'Alex Rodriguez', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), DATEADD('HOUR', -1, CURRENT_TIMESTAMP)),

(2, 0, 'Ticket created - Outlook attachment issues', 'system', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), DATEADD('HOUR', -1, CURRENT_TIMESTAMP)),
(2, 0, 'Assigned to technician: Jamie Kim', 'admin', DATEADD('MINUTE', -45, CURRENT_TIMESTAMP), DATEADD('MINUTE', -45, CURRENT_TIMESTAMP)),

(3, 0, 'Ticket created - Laptop overheating issue', 'system', DATEADD('MINUTE', -30, CURRENT_TIMESTAMP), DATEADD('MINUTE', -30, CURRENT_TIMESTAMP)),
(3, 0, 'Assigned to technician: Morgan Foster', 'admin', DATEADD('MINUTE', -20, CURRENT_TIMESTAMP), DATEADD('MINUTE', -20, CURRENT_TIMESTAMP)),

-- History for overdue tickets
(6, 0, 'Ticket created - Adobe licensing error', 'system', DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(6, 0, 'Assigned to technician: Morgan Foster', 'admin', DATEADD('HOUR', -60, CURRENT_TIMESTAMP), DATEADD('HOUR', -60, CURRENT_TIMESTAMP)),
(6, 0, 'Escalated - ticket overdue, customer follow-up needed', 'admin', DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP)),

-- History for closed tickets
(8, 0, 'Ticket created - Monitor flickering issue', 'system', DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(8, 0, 'Assigned to technician: Alex Rodriguez', 'admin', DATEADD('HOUR', -23, CURRENT_TIMESTAMP), DATEADD('HOUR', -23, CURRENT_TIMESTAMP)),
(8, 0, 'Diagnosed faulty VGA cable, replacement ordered', 'Alex Rodriguez', DATEADD('HOUR', -22, CURRENT_TIMESTAMP), DATEADD('HOUR', -22, CURRENT_TIMESTAMP)),
(8, 1, 'Ticket closed - Resolution: Replaced faulty VGA cable, monitor working normally', 'Alex Rodriguez', DATEADD('HOUR', -21, CURRENT_TIMESTAMP), DATEADD('HOUR', -21, CURRENT_TIMESTAMP)),

(9, 0, 'Ticket created - Windows update stuck', 'system', DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(9, 0, 'Assigned to technician: Jamie Kim', 'admin', DATEADD('HOUR', -46, CURRENT_TIMESTAMP), DATEADD('HOUR', -46, CURRENT_TIMESTAMP)),
(9, 1, 'Ticket closed - Resolution: Ran Windows update troubleshooter, updates completed successfully', 'Jamie Kim', DATEADD('HOUR', -43, CURRENT_TIMESTAMP), DATEADD('HOUR', -43, CURRENT_TIMESTAMP));

-- ===============================
-- FEEDBACK ENTRIES
-- ===============================
INSERT INTO feedback_entries (ticket_id, rating, comment, created_by, submitted_at) VALUES
-- Feedback for closed tickets
(8, 5, 'Excellent service! Alex was very professional and fixed the issue quickly. Monitor is working perfectly now.', 'John Doe', DATEADD('HOUR', -20, CURRENT_TIMESTAMP)),
(9, 4, 'Good service, though it took a bit longer than expected. Jamie explained the process clearly.', 'Sarah Johnson', DATEADD('HOUR', -41, CURRENT_TIMESTAMP)),
(10, 5, 'Outstanding work! Morgan was very thorough and even gave me tips to prevent future issues.', 'Mike Chen', DATEADD('HOUR', -67, CURRENT_TIMESTAMP)),
(11, 3, 'Service was okay. Issue was resolved but communication could have been better.', 'Emily Davis', DATEADD('HOUR', -91, CURRENT_TIMESTAMP)),
(12, 4, 'Satisfied with the resolution. Alex was knowledgeable and the fix was permanent.', 'Robert Wilson', DATEADD('HOUR', -115, CURRENT_TIMESTAMP)),
(13, 5, 'Perfect! Morgan understood the software issue immediately and provided a comprehensive solution.', 'Lisa Martinez', DATEADD('HOUR', -139, CURRENT_TIMESTAMP)),
(15, 2, 'Resolution took too long and multiple follow-ups were needed. Could be improved.', 'Jennifer Taylor', DATEADD('HOUR', -187, CURRENT_TIMESTAMP)); 