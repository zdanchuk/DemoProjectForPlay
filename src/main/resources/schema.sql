-- Tworzenie tabeli użytkowników
CREATE TABLE IF NOT EXISTS  users (
                       id SERIAL PRIMARY KEY, -- Unikalny identyfikator użytkownika
                       phone_number VARCHAR(20) UNIQUE NOT NULL, -- Numer telefonu użytkownika
                       status VARCHAR(10) CHECK (status IN ('ACTIVE', 'INACTIVE')) NOT NULL, -- Status usługi dla użytkownika
                       message_checked INT DEFAULT 0, -- Liczba sprawdzonych wiadomości
                       paid_messages INT DEFAULT 0 -- Liczba opłaconych wiadomości
);

-- Tworzenie tabeli szablonów SMS
CREATE TABLE IF NOT EXISTS  sms_templates (
                               id SERIAL PRIMARY KEY, -- Unikalny identyfikator szablonu SMS
                               template_key VARCHAR(50) UNIQUE NOT NULL, -- Klucz identyfikujący szablon
                               message TEXT NOT NULL -- Treść wiadomości SMS
);