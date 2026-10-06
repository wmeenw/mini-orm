import model.Student;
import orm.repository.DBConfig;
import orm.repository.EntityManager;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = DBConfig.getConnection()) {
            EntityManager entityManager = new EntityManager(connection);
            entityManager.createTable(Student.class);

            Student st1 = new Student("Ivan", 18, 11);
            Long id1 = entityManager.save(st1);
            System.out.println(id1);

            Student st2 = new Student("Eve", 7, 1);
            Long id2 = entityManager.save(st2);
            System.out.println(id2);

            Student st3 = new Student("Kate", 15, 7);
            Long id3 = entityManager.save(st3);
            System.out.println(id3);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
