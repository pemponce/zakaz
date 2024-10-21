#!/bin/bash

# Задать пароль как переменную окружения
export PGPASSWORD="a785410a"
echo suc1
# Войти в контейнер и выполнить psql команду
docker exec -i postgres_bot /bin/bash -c "
export PGPASSWORD='${PGPASSWORD}' && \
psql -h postgres -p 5432 -U postgres -d questionbot -c 'UPDATE users SET role=0 WHERE id=1;'
"
echo suc2
unset PGPASSWORD
echo suc3