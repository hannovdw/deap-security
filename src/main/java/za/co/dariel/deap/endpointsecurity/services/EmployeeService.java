package za.co.dariel.deap.endpointsecurity.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.dariel.deap.endpointsecurity.entities.EmployeeEntity;
import za.co.dariel.deap.endpointsecurity.repository.EmployeeRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class EmployeeService implements UserDetailsService {

	private EmployeeRepository employeeRepo;

	private final static String userNotFoundMessage = "EmployeeEntity with username: %s not found";

	private KeycloakService keyClockService;
	
	private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return employeeRepo.findById(username)
				.orElseThrow(() -> new UsernameNotFoundException(String.format(userNotFoundMessage, username)));
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

		if (Boolean.TRUE.equals(userExists)) {
			throw new IllegalStateException("Email is already taken");

		}
		String userId = keyClockService.createUserInKeyCloak(employee);
		employee.setKeycloakId(userId);
		logger.error(employee.getPassword());

		return employeeRepo.save(employee);

	}

	@Transactional
	// annotation allows me to just use the setters to adjust values in database
	// instead of having to ask repo to save again
	public EmployeeEntity updateStudent(String username, EmployeeEntity employeeRequest) {

		EmployeeEntity employee = employeeRepo.findById(username).orElseThrow(
				() -> new IllegalStateException("EmployeeEntity with username: " + username + " does not exist"));

		if (employeeRequest.getFirstName() != null && employeeRequest.getFirstName().length() > 0
				&& !Objects.equals(employee.getFirstName(), employeeRequest.getFirstName())) {
			employee.setFirstName(employeeRequest.getFirstName());
		}

		if (employeeRequest.getLastName() != null && employeeRequest.getLastName().length() > 0
				&& !Objects.equals(employee.getLastName(), employeeRequest.getLastName())) {
			employee.setLastName(employeeRequest.getLastName());
		}
		return employeeRequest;
	}

	public void deleteEmployee(String userId) {
		try {
			EmployeeEntity employee = employeeRepo.findByKeycloakId(userId);
			employeeRepo.deleteById(employee.getEmail());
		}
		catch(Exception e){
			logger.error("EmployeeEntity with keycloak id: " + userId + " does not exist");
		}

	}

}
