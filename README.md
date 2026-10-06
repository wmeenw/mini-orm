# Mini ORM

Собственная маленькая ORM на Java и JDBC. Сущности описываются аннотациями, таблицы создаются по классам, а объекты сохраняются и читаются через `EntityManager`. В качестве базы используется H2 в памяти.

Домашнее задание 7 по Java.

## Возможности

- Аннотации: `@Table`, `@Column`, `@Id`.
- Создание таблицы по классу: `createTable`.
- CRUD: `save`, `saveAll`, `update`, `delete`, `deleteById`.
- Чтение: `findById`, `findAll`, `findAllWhere`, `findOneWhere`.
- Утилиты: `count`, `existsById`.
- Метаданные сущностей через рефлексию (`EntityMetadata`).

## Пример

```java
@Table(name = "students")
public class Student {
    @Id
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    // поля, конструкторы, геттеры
}
```

```java
try (Connection connection = DBConfig.getConnection()) {
    EntityManager em = new EntityManager(connection);
    em.createTable(Student.class);

    em.save(new Student("Ivan", 18, 11, LocalDate.of(2020, 10, 11)));

    Optional<Student> found = em.findById(Student.class, 1L);
    List<Student> all = em.findAll(Student.class);
    long total = em.count(Student.class);
}
```

## Подключение к базе

`DBConfig` открывает соединение с H2 в памяти:

```text
jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1   пользователь: sa, пароль пустой
```

Данные живут, пока работает приложение.

## Структура

```
homework7/
├── pom.xml                      родительский POM
├── orm/                         библиотека
│   └── src/main/java/orm/
│       ├── annotation/          Table, Column, Id
│       ├── core/                EntityMetadata, OrmException
│       └── repository/          DBConfig, EntityManager
├── homework-orm-6/              демо-модуль с Student
├── homework-orm-8/              демо-модуль с Student
└── homework-orm-9/              демо-модуль с Book
```

## Сборка и запуск

Модуль `orm` нужно установить в локальный Maven-репозиторий до сборки демо-модулей:

```bash
cd homework7/orm
mvn clean install
cd ..
mvn clean package
```

Запускайте `Main` в нужном модуле `homework-orm-N` из IntelliJ IDEA.

## Требования

- JDK 25 (в `pom.xml` указан `maven.compiler.source` 25).
- Maven 3.9+.

## Автор

Мария Комарова — домашнее задание 7 по Java.

## Авторство

Исходный код и решения написаны автором репозитория (Мария Комарова) для курса по Java и взяты из ветки `homework7` репозитория [wmeenw/JAVAhomework](https://github.com/wmeenw/JAVAhomework).

Оформление репозитория (структура, README, публикация на GitHub) выполнено с помощью Claude Code (Anthropic).

