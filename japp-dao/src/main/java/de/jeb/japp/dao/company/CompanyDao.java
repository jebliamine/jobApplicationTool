package de.jeb.japp.dao.company;

import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.user.User;
import de.jeb.japp.repositories.CompanyRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CompanyDao {

    private final CompanyRepository companyRepository;

    public CompanyDao(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public List<Company> getAllCompaniesByOwner(User owner) {
        return companyRepository.findByOwner(owner);
    }

    public Optional<Company> getCompanyById(UUID id) {
        return companyRepository.findById(id);
    }

    public Company saveCompany(Company company) {
        return companyRepository.save(company);
    }

    public void deleteCompany(UUID id) {
        companyRepository.deleteById(id);
    }
}
