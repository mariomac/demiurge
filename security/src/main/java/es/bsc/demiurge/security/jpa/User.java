package es.bsc.demiurge.security.jpa;

import javax.persistence.*;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Entity
@Table(name = "USERS",
		indexes = {
			@Index(name = "IDX_USER_NAME", unique = true, columnList = "NAME")
		}
)
public class User {
	@Id
	@GeneratedValue
	@Column(name="ID", updatable = false)
	private Integer id;

	@Column(name = "NAME", unique = true, length = MAX_LENGTH_NAME, updatable = false, nullable = false)
	private String name;


	@Column(name = "CRYPTED_PASSWORD", nullable = false, length = MAX_LENGTH_CIPHERED_PASSWORD)
	private byte[] cryptedPassword;



	private static final int MAX_LENGTH_NAME = 64;
	private static final int MAX_LENGTH_CIPHERED_PASSWORD = 1024;
}
