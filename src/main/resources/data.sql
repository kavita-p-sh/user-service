INSERT INTO tb_roles (role_name)
SELECT 'USER'
WHERE NOT EXISTS (
    SELECT 1 FROM tb_roles WHERE role_name = 'USER'
);

INSERT INTO tb_roles (role_name)
SELECT 'ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM tb_roles WHERE role_name = 'ADMIN'
);

INSERT INTO tb_roles (role_name)
SELECT 'MANAGER'
WHERE NOT EXISTS (
    SELECT 1 FROM tb_roles WHERE role_name = 'MANAGER'
);