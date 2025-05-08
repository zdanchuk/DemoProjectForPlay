-- 📌 Wstawianie szablonów SMS, ale tylko jeśli tabela `sms_templates` nie zawiera żadnych rekordów
INSERT INTO sms_templates (template_key, message)
SELECT 'activation_template', 'Twoja usługa została aktywowana!' UNION ALL  -- Wiadomość aktywacyjna
SELECT 'reactivation_template', 'Twoja usługa była wcześniej aktywowana i nadal działa.' UNION ALL  -- Informacja o reaktywacji
SELECT 'already_active_template', 'Twoja usługa jest już aktywna.' UNION ALL  -- Usługa już aktywna
SELECT 'deactivation_template', 'Twoja usługa została dezaktywowana.' UNION ALL  -- Wiadomość o dezaktywacji
SELECT 'already_inactive_template', 'Twoja usługa była już dezaktywowana.' UNION ALL  -- Usługa już była nieaktywna
SELECT 'not_active_template', 'Usługa nigdy nie była aktywowana.'  -- Usługa nigdy nie została uruchomiona
ON CONFLICT (template_key) DO NOTHING;


-- Dodanie przykładowych użytkowników do testowania
INSERT INTO users (phone_number, status, message_checked, paid_messages)
SELECT '123456789', 'ACTIVE', 5, 3 UNION ALL    -- Użytkownik aktywny, 5 sprawdzonych wiadomości, 3 opłacone
SELECT '987654321', 'INACTIVE', 2, 2    -- Użytkownik nieaktywny, 2 sprawdzone wiadomości, 2 opłacone
ON CONFLICT (phone_number) DO NOTHING;