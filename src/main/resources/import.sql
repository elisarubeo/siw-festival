-- Dati di prova caricati automaticamente da Hibernate all'avvio, dopo la
-- creazione dello schema (funziona finche' ddl-auto=create o create-drop).
-- ATTENZIONE: ogni istruzione deve stare su una sola riga.

-- ============================== REGISTI ==============================
insert into director (id, name, surname, nationality, birth_date) values (1, 'Paolo', 'Sorrentino', 'Italiana', '1970-05-31');
insert into director (id, name, surname, nationality, birth_date) values (2, 'Matteo', 'Garrone', 'Italiana', '1968-10-15');
insert into director (id, name, surname, nationality, birth_date) values (3, 'Alice', 'Rohrwacher', 'Italiana', '1981-12-29');
insert into director (id, name, surname, nationality, birth_date) values (4, 'Wim', 'Wenders', 'Tedesca', '1945-08-14');
insert into director (id, name, surname, nationality, birth_date) values (5, 'Celine', 'Sciamma', 'Francese', '1978-11-12');

-- =============================== FILM ================================
insert into movie (id, title, genre, year, country, duration, director_id) values (1, 'La grande bellezza', 'Drammatico', 2013, 'Italia', 141, 1);
insert into movie (id, title, genre, year, country, duration, director_id) values (2, 'E'' stata la mano di Dio', 'Drammatico', 2021, 'Italia', 130, 1);
insert into movie (id, title, genre, year, country, duration, director_id) values (3, 'Il racconto dei racconti', 'Fantastico', 2015, 'Italia', 133, 2);
insert into movie (id, title, genre, year, country, duration, director_id) values (4, 'Dogman', 'Drammatico', 2018, 'Italia', 103, 2);
insert into movie (id, title, genre, year, country, duration, director_id) values (5, 'Lazzaro felice', 'Drammatico', 2018, 'Italia', 130, 3);
insert into movie (id, title, genre, year, country, duration, director_id) values (6, 'La chimera', 'Drammatico', 2023, 'Italia', 130, 3);
insert into movie (id, title, genre, year, country, duration, director_id) values (7, 'Il cielo sopra Berlino', 'Fantastico', 1987, 'Germania', 128, 4);
insert into movie (id, title, genre, year, country, duration, director_id) values (8, 'Ritratto della giovane in fiamme', 'Drammatico', 2019, 'Francia', 122, 5);

-- ============================= FESTIVAL ==============================
insert into festival (id, name, city, year, start_date, end_date, description) values (1, 'Festival del Cinema Italiano', 'Roma', 2026, '2026-09-10', '2026-09-20', 'Dieci giorni dedicati al meglio della produzione italiana recente, con incontri e retrospettive d''autore.');
insert into festival (id, name, city, year, start_date, end_date, description) values (2, 'Rassegna Autori Europei', 'Bologna', 2026, '2026-10-05', '2026-10-11', 'Una settimana di cinema d''autore europeo, tra classici restaurati e nuove uscite.');
insert into festival (id, name, city, year, start_date, end_date, description) values (3, 'Notti di Cinema', 'Napoli', 2025, '2025-07-01', '2025-07-15', 'Proiezioni serali all''aperto sul lungomare, con ingresso libero.');

-- =============================== SALE ================================
insert into theater (id, name, address, capacity) values (1, 'Sala Fellini', 'Via Roma 1, Roma', 200);
insert into theater (id, name, address, capacity) values (2, 'Sala Visconti', 'Via Nazionale 45, Roma', 120);
insert into theater (id, name, address, capacity) values (3, 'Cinema Odeon', 'Piazza Maggiore 3, Bologna', 300);
insert into theater (id, name, address, capacity) values (4, 'Arena Estiva', 'Lungomare Caracciolo 10, Napoli', 500);

-- ================== PARTECIPAZIONE FILM AI FESTIVAL ==================
insert into movie_festival (movie_id, festival_id) values (1, 1);
insert into movie_festival (movie_id, festival_id) values (2, 1);
insert into movie_festival (movie_id, festival_id) values (3, 1);
insert into movie_festival (movie_id, festival_id) values (4, 1);
insert into movie_festival (movie_id, festival_id) values (5, 1);
insert into movie_festival (movie_id, festival_id) values (6, 2);
insert into movie_festival (movie_id, festival_id) values (7, 2);
insert into movie_festival (movie_id, festival_id) values (8, 2);
insert into movie_festival (movie_id, festival_id) values (1, 3);
insert into movie_festival (movie_id, festival_id) values (7, 3);

-- ============================ PROIEZIONI =============================
insert into screening (id, screening_date, start_time, status, festival_id, movie_id, theater_id) values (1, '2026-09-10', '18:00', 'SCHEDULED', 1, 1, 1);
insert into screening (id, screening_date, start_time, status, festival_id, movie_id, theater_id) values (2, '2026-09-10', '21:00', 'SCHEDULED', 1, 2, 1);
insert into screening (id, screening_date, start_time, status, festival_id, movie_id, theater_id) values (3, '2026-09-11', '18:30', 'SCHEDULED', 1, 3, 2);
insert into screening (id, screening_date, start_time, status, festival_id, movie_id, theater_id) values (4, '2026-09-11', '21:30', 'SCHEDULED', 1, 4, 2);
insert into screening (id, screening_date, start_time, status, festival_id, movie_id, theater_id) values (5, '2026-09-12', '17:00', 'SCHEDULED', 1, 1, 2);
insert into screening (id, screening_date, start_time, status, festival_id, movie_id, theater_id) values (6, '2026-09-12', '20:00', 'SCHEDULED', 1, 5, 1);
insert into screening (id, screening_date, start_time, status, festival_id, movie_id, theater_id) values (7, '2026-10-05', '19:00', 'SCHEDULED', 2, 6, 3);
insert into screening (id, screening_date, start_time, status, festival_id, movie_id, theater_id) values (8, '2026-10-06', '21:00', 'SCHEDULED', 2, 7, 3);
insert into screening (id, screening_date, start_time, status, festival_id, movie_id, theater_id) values (9, '2026-10-07', '18:00', 'SCHEDULED', 2, 8, 3);
insert into screening (id, screening_date, start_time, status, festival_id, movie_id, theater_id) values (10, '2025-07-02', '21:30', 'COMPLETED', 3, 1, 4);
insert into screening (id, screening_date, start_time, status, festival_id, movie_id, theater_id) values (11, '2025-07-05', '21:30', 'COMPLETED', 3, 7, 4);
insert into screening (id, screening_date, start_time, status, festival_id, movie_id, theater_id) values (12, '2025-07-08', '21:30', 'CANCELLED', 3, 1, 4);

-- ============================== UTENTI ===============================
-- User contiene solo il profilo; username, password e ruolo stanno in Credentials.
insert into users (id, name, surname) values (1, 'Anna', 'Bianchi');
insert into users (id, name, surname) values (2, 'Elisa', 'Rubeo');
insert into users (id, name, surname) values (3, 'Marco', 'Verdi');

-- ============================ CREDENZIALI ============================
-- Password cifrate con BCrypt: admin/admin, elisa/password, marco/password.
-- Il PasswordEncoder cifra solo quando si registra dall'applicazione,
-- quindi gli hash inseriti via SQL vanno generati a parte.
insert into credentials (id, username, password, role, user_id) values (1, 'admin', '$2a$10$25KyureLZaPhBw2ZSVRrDeNWrwmUONheHNxgVspmL/n6rpr1PAnLi', 'ADMIN', 1);
insert into credentials (id, username, password, role, user_id) values (2, 'elisa', '$2a$10$N5z1e9AdGpc2DWL7eiJ2GuZvwkiVh5Y9vPI2KqTOV7235fhfqFSbu', 'DEFAULT', 2);
insert into credentials (id, username, password, role, user_id) values (3, 'marco', '$2a$10$vgRJAtBbDHN/g2ITv6tFYeuwrpTUXO0i4CDfw44YCV6Nmu5EUKFWe', 'DEFAULT', 3);

-- ============================ RECENSIONI =============================
insert into review (id, text, rating, review_date, movie_id, user_id) values (1, 'Un affresco malinconico e sontuoso su Roma. La fotografia da sola vale la visione.', 5, '2025-11-02', 1, 2);
insert into review (id, text, rating, review_date, movie_id, user_id) values (2, 'Visivamente straordinario, ma il ritmo cala nella seconda parte.', 4, '2025-11-08', 1, 3);
insert into review (id, text, rating, review_date, movie_id, user_id) values (3, 'Il film piu'' personale del regista: doloroso e tenerissimo allo stesso tempo.', 5, '2025-12-01', 2, 2);
insert into review (id, text, rating, review_date, movie_id, user_id) values (4, 'Una favola contemporanea sul candore e sullo sfruttamento. Da rivedere.', 4, '2026-01-14', 5, 3);
insert into review (id, text, rating, review_date, movie_id, user_id) values (5, 'Poetico ma lento, non per tutti i gusti.', 3, '2026-02-20', 7, 2);
insert into review (id, text, rating, review_date, movie_id, user_id) values (6, 'La tensione dello sguardo e'' costruita in modo impeccabile.', 5, '2026-03-05', 8, 3);

-- Le sequenze ripartono ben sopra gli id usati qui, cosi' i prossimi
-- inserimenti fatti da Hibernate non collidono con questi dati.
alter sequence director_seq restart with 1000;
alter sequence movie_seq restart with 1000;
alter sequence festival_seq restart with 1000;
alter sequence theater_seq restart with 1000;
alter sequence screening_seq restart with 1000;
alter sequence users_seq restart with 1000;
alter sequence credentials_seq restart with 1000;
alter sequence review_seq restart with 1000;
