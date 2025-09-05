-- Simple operator assignment system setup script
USE happy_tassie_travel;

-- Add fields to employees table
ALTER TABLE employees ADD COLUMN operator_type VARCHAR(20) DEFAULT 'general';
ALTER TABLE employees ADD COLUMN is_tour_master BOOLEAN DEFAULT FALSE;
ALTER TABLE employees ADD COLUMN can_assign_orders BOOLEAN DEFAULT FALSE;

-- Add fields to tour_booking table
ALTER TABLE tour_booking ADD COLUMN assigned_operator_id BIGINT DEFAULT NULL;
ALTER TABLE tour_booking ADD COLUMN assigned_at DATETIME DEFAULT NULL;
ALTER TABLE tour_booking ADD COLUMN assigned_by BIGINT DEFAULT NULL;
ALTER TABLE tour_booking ADD COLUMN assignment_status VARCHAR(20) DEFAULT 'unassigned';

-- Create operator_assignments table
CREATE TABLE operator_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    operator_id BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    assignment_type VARCHAR(20) DEFAULT 'hotel_management',
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'active',
    transferred_to BIGINT DEFAULT NULL,
    transferred_at DATETIME DEFAULT NULL,
    notes TEXT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_booking_id (booking_id),
    INDEX idx_operator_id (operator_id),
    INDEX idx_assigned_by (assigned_by),
    INDEX idx_status (status),
    FOREIGN KEY (booking_id) REFERENCES tour_booking(bookingId) ON DELETE CASCADE,
    FOREIGN KEY (operator_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES employees(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create duty_shifts table
CREATE TABLE duty_shifts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    duty_type VARCHAR(20) NOT NULL,
    shift_start DATETIME NOT NULL,
    shift_end DATETIME DEFAULT NULL,
    status VARCHAR(20) DEFAULT 'active',
    transferred_to BIGINT DEFAULT NULL,
    transferred_at DATETIME DEFAULT NULL,
    transfer_reason VARCHAR(200) DEFAULT NULL,
    notes TEXT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_operator_id (operator_id),
    INDEX idx_duty_type (duty_type),
    INDEX idx_status (status),
    FOREIGN KEY (operator_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create indexes for new fields
CREATE INDEX idx_employees_operator_type ON employees(operator_type);
CREATE INDEX idx_employees_is_tour_master ON employees(is_tour_master);
CREATE INDEX idx_tour_booking_assigned_operator ON tour_booking(assigned_operator_id);
CREATE INDEX idx_tour_booking_assignment_status ON tour_booking(assignment_status);

-- Set initial tour master (employees id=2)
UPDATE employees SET 
    operator_type = 'tour_master',
    is_tour_master = TRUE,
    can_assign_orders = TRUE
WHERE id = 2;
