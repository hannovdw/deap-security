package za.co.dariel.deap.endpointsecurity.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import za.co.dariel.deap.endpointsecurity.entities.EmployeeEntity;
import za.co.dariel.deap.endpointsecurity.repository.EmployeeRepository;

@Service
@AllArgsConstructor
public class EmployeeService implements UserDetailsService{

	private EmployeeRepository employeeRepo;
	
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	
	private final static String USER_NOT_FOUND_MSG = "EmployeeEntity with username: %s not found";
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return employeeRepo.findById(username).orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, username)));
	}
	
	

	public List<EmployeeEntity> getEmployees() {

		return (List<EmployeeEntity>) employeeRepo.findAll();
	}

	public EmployeeEntity getSingleEmployee(String username) {
		Optional<EmployeeEntity> email = employeeRepo.findById(username);

		if (!email.isPresent()) {
			throw new IllegalStateException("Username/Account does not exist     ");
		}

		return employeeRepo.findById(username).orElse(null);
	}

	public EmployeeEntity addEmployee(EmployeeEntity employee) {
		Boolean userExists = employeeRepo.findById(employee.getEmail()).isPresent();
		
		if(userExists) {
			throw new IllegalStateException("Email is already taken");
	
		}
		
		String givenPassword = employee.getPassword();
		
		String encodedPassword = bCryptPasswordEncoder.encode(employee.getPassword());
		
		employee.setPassword(encodedPassword);
		
		//code below just tests to make sure password was hashed correctly and matches original
		boolean isPasswordMatch = bCryptPasswordEncoder.matches(givenPassword, encodedPassword);
		System.out.println("Password : " + givenPassword + "   isPasswordMatch    : " + isPasswordMatch);
		
		return employeeRepo.save(employee);

	}

	@Transactional
	//annotation allows me to just use the setters to adjust values in database instead of having to ask repo to save again
	public void updateStudent(String username, String firstName, String lastName) {

		EmployeeEntity employee = employeeRepo.findById(username)
				.orElseThrow(() -> new IllegalStateException("EmployeeEntity with username: " + username + " does not exist"));

		if (firstName != null && firstName.length() > 0 && !Objects.equals(employee.getFirstName(), firstName)) {
			employee.setFirstName(firstName);
		}

		if (lastName != null && lastName.length() > 0 && !Objects.equals(employee.getLastName(), lastName)) {
			employee.setLastName(lastName);
		}
	}

	

}