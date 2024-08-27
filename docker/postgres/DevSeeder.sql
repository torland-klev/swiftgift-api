INSERT INTO users (id, "firstName", "lastName", email)
VALUES ('9476a623-a15f-4107-bf35-4d85c2bf8a35', 'Henrik', 'Klev', 'klevhenrik@gmail.com');

INSERT INTO googleusers (id, name, "givenName", "familyName", email, "verifiedEmail", picture)
VALUES ('115784667685649850612', 'Henrik Klev', 'Henrik', 'Klev', 'klevhenrik@gmail.com', true, 'https://lh3.googleusercontent.com/a/ACg8ocJh_LiFbbzW2hIc5Za5yjc_6kIB2edXEwt95MBDc4AMuVdh-A=s96-c');

INSERT INTO userstogoogleusers (id, "userId", "googleUserId", "authToken")
VALUES ('a82a1adf-f0a5-4424-8aac-07806e432345', '9476a623-a15f-4107-bf35-4d85c2bf8a35', '115784667685649850612', 'ya29');

INSERT INTO groups (id, name, "createdBy", visibility)
VALUES ('5c3caeb1-4389-44d4-a993-62dd60d6961e', 'Family', '9476a623-a15f-4107-bf35-4d85c2bf8a35', 'PRIVATE'),
       ('51a51d87-f4bc-469c-b7bd-d82d2349757b', 'Charity Fund', '9476a623-a15f-4107-bf35-4d85c2bf8a35', 'PUBLIC'),
       ('51a51d87-f4bc-469c-b7bd-d82d2349757c', 'Cory & Anita''s Wedding', '9476a623-a15f-4107-bf35-4d85c2bf8a35', 'PRIVATE');

INSERT INTO groupmemberships (id, "userId", "groupId", role)
VALUES ('0d2724bb-4f3d-4d6e-b2bc-e288e70995a2', '9476a623-a15f-4107-bf35-4d85c2bf8a35', '5c3caeb1-4389-44d4-a993-62dd60d6961e', 'ADMIN'),
       ('0d2724bb-4f3d-4d6e-b2bc-e288e70995b3', '9476a623-a15f-4107-bf35-4d85c2bf8a35', '51a51d87-f4bc-469c-b7bd-d82d2349757b', 'MEMBER'),
       ('0d2724bb-4f3d-4d6e-b2bc-e288e70995c4', '9476a623-a15f-4107-bf35-4d85c2bf8a35', '51a51d87-f4bc-469c-b7bd-d82d2349757c', 'OWNER');

INSERT INTO wishes (id, "userId", occasion, status, visibility, title, url, description, img)
VALUES ('7a29ed01-1709-4f39-8ff0-1d6b7b661baf', '9476a623-a15f-4107-bf35-4d85c2bf8a35', 'BIRTHDAY', 'OPEN', 'GROUP', 'Salah Kit XL', null, '2025 Home Kit', 'e25b5651-41f0-4704-b5da-6577c79e8243'),
       ('7a29ed01-1709-4f39-8ff0-1d6b7b661bbf', '9476a623-a15f-4107-bf35-4d85c2bf8a35', 'GRADUATION', 'OPEN', 'PRIVATE', 'Porsche 911', null, 'Turbo S', 'e25b5651-41f0-4704-b5da-6577c79e8244');

INSERT INTO groupstowishes (id, "groupId", "wishId")
VALUES ('ded327a8-cffc-4bd5-a02f-0f30aba0626e', '5c3caeb1-4389-44d4-a993-62dd60d6961e', '7a29ed01-1709-4f39-8ff0-1d6b7b661baf');
