package mk.ukim.finki.wp.kol2022.g1.service.impl;

import mk.ukim.finki.wp.kol2022.g1.model.Employee;
import mk.ukim.finki.wp.kol2022.g1.model.EmployeeType;
import mk.ukim.finki.wp.kol2022.g1.model.Skill;
import mk.ukim.finki.wp.kol2022.g1.model.exceptions.InvalidEmployeeIdException;
import mk.ukim.finki.wp.kol2022.g1.model.exceptions.InvalidSkillIdException;
import mk.ukim.finki.wp.kol2022.g1.repository.EmployeeRepository;
import mk.ukim.finki.wp.kol2022.g1.repository.SkillRepository;
import mk.ukim.finki.wp.kol2022.g1.service.EmployeeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SkillRepository skillRepository;


    public EmployeeServiceImpl(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, SkillRepository skillRepository) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.skillRepository = skillRepository;
    }

    @Override
    public List<Employee> listAll() {
        return this.employeeRepository.findAll();
    }

    @Override
    public Employee findById(Long id) {
        return this.employeeRepository.findById(id).orElseThrow(InvalidEmployeeIdException::new);
    }

    @Override
    public Employee create(String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        List<Skill> skills = this.skillRepository.findAllById(skillId);
        Employee e = new Employee(name, email, passwordEncoder.encode(password), type, skills, employmentDate);
        return this.employeeRepository.save(e);
    }

    @Override
    public Employee update(Long id, String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        List<Skill> skills = this.skillRepository.findAllById(skillId);
        Employee e = this.findById(id);
        e.setSkills(skills);
        e.setName(name);
        e.setPassword(passwordEncoder.encode(password));
        e.setEmail(email);
        e.setType(type);
        e.setEmploymentDate(employmentDate);

        return this.employeeRepository.save(e);
    }

    @Override
    public Employee delete(Long id) {
        Employee e = this.findById(id);
        this.employeeRepository.delete(e);
        return e;
    }

    @Override
    public List<Employee> filter(Long skillId, Integer yearsOfService) {
        if(skillId == null && yearsOfService == null){
            return this.listAll();
        }
        else if(skillId == null){
            return this.employeeRepository.findAllByEmploymentDateBefore(LocalDate.now().minusYears(yearsOfService));
        }
        else if(yearsOfService == null){
            return this.employeeRepository.findAllBySkillsContaining(this.skillRepository.findById(skillId).orElseThrow(InvalidSkillIdException::new));
        }
        else {
            return this.employeeRepository.findAllBySkillsContainingAndEmploymentDateBefore(this.skillRepository.findById(skillId).orElseThrow(InvalidSkillIdException::new),LocalDate.now().minusYears(yearsOfService));
        }
    }
}
