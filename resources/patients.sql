-- :name create-patients-table
-- :command :execute
-- :result :raw
-- :doc creates patients table
CREATE TABLE IF NOT EXISTS patients (
       id SERIAL PRIMARY KEY,
       version INTEGER NOT NULL DEFAULT 0,
       created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
       modified_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
       is_active BOOLEAN NOT NULL DEFAULT true,
       data JSONB NOT NULL
);

-- :name drop-patients-table
-- :command :execute
-- :result :raw
-- :doc drops patients table
DROP TABLE IF EXISTS patients;

-- :name get-active-patients :? :*
SELECT *
FROM patients
WHERE is_active;

-- :name get-active-patient-by-id :? :1
SELECT *
FROM patients
WHERE id = :id
AND is_active;

-- :name insert-patient :insert :1
INSERT INTO patients (data)
VALUES (:data::jsonb)
RETURNING id;

-- :name update-whole-patient :! :1
UPDATE patients
SET data = :data::jsonb, version = :version + 1, modified_at = current_timestamp
WHERE id = :id
AND version = :version

-- :name deactivate-patient :! :1
UPDATE patients
SET is_active = false, modified_at = current_timestamp
WHERE id = :id;

-- :name activate-patient :! :1
UPDATE patients
SET is_active = true, modified_at = current_timestamp
WHERE id = :id;
