package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.transaction.Transactional;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Autowired
	MyEntityRepository myEntityRepository;

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		MyEntity save = myEntityRepository.save(new MyEntity("name"));
		System.out.println("Saved [" + save + "]");
		MyEntity read = myEntityRepository.getById(save.getId());
		System.out.println("Read [" + read + "]");
		Assert.isTrue(save.getId().equals(read.getId()), "Entities should be the same");
	}

}

@Entity
class MyEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;

	public MyEntity() {

	}

	public MyEntity(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "MyEntity{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}

@Repository
interface MyEntityRepository extends JpaRepository<MyEntity, Long> {

}
