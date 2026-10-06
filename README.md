# Mini ORM

Домашнее задание 7 по Java. Собственная мини-ORM на JDBC с базой H2.

## Что сделано в качестве ДЗ

- Библиотека `orm/`: аннотации `@Table`, `@Column`, `@Id`.
- Чтение метаданных сущностей через рефлексию (`EntityMetadata`).
- `EntityManager` для сохранения и чтения сущностей через JDBC.
- Подключение к H2 (`DBConfig`), исключения (`OrmException`).
- Демо-модули `homework-orm-6`, `homework-orm-8`, `homework-orm-9` с сущностями `Student` и `Book`.

## Сборка и запуск

```bash
cd orm && mvn clean install && cd ..
mvn clean package
```

Для запуска используйте `Main` в нужном модуле `homework-orm-N`.

Исходный код: ветка `homework7` репозитория [wmeenw/JAVAhomework](https://github.com/wmeenw/JAVAhomework).
