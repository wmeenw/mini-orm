package model;

import orm.annotation.Column;
import orm.annotation.Id;
import orm.annotation.Table;

import java.time.LocalDate;

@Table(name = "students")
public class Student {
    @Id
    private Long id;
    @Column
    private String name;
    @Column
    private int age;
    @Column
    private double grade;
    @Column
    private LocalDate enrollmentDate;

    public Student(){}

    public Student(String name, int age, double grade, LocalDate enrollmentDate) {
        this.name = name;
        this.age = age;
        this.grade = grade;
        this.enrollmentDate = enrollmentDate;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }
    public double getGrade() {
        return grade;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public void setGrade(double grade) {
        this.grade = grade;
    }
    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
}
