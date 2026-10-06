# Домашнее задание 7 — мини-ORM

Мини-ORM на Java и JDBC с базой H2.

- `orm/` — сама библиотека:
  - аннотации `@Table`, `@Column`, `@Id`;
  - `EntityMetadata` — чтение метаданных сущностей через рефлексию;
  - `EntityManager` — сохранение и чтение сущностей;
  - `DBConfig` — подключение к базе.
- `homework-orm-6`, `homework-orm-8`, `homework-orm-9` — демонстрационные модули (сущности `Student` и `Book`, каждый со своим `Main`).

## Сборка и запуск

Сборка из корня (модуль `orm` устанавливается в локальный Maven-репозиторий, затем собираются демо-модули):

```bash
cd orm && mvn clean install && cd ..
mvn clean package
```

Для запуска используйте `Main` в нужном модуле `homework-orm-N`.

Исходный код взят из ветки `homework7` репозитория [wmeenw/JAVAhomework](https://github.com/wmeenw/JAVAhomework).
