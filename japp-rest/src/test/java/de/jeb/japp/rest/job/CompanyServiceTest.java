package de.jeb.japp.rest.job;

import de.jeb.japp.dao.company.CompanyDao;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.company.dto.CompanyRequest;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyDao companyDao;
    @Mock
    private JobDao jobDao;

    private CompanyService companyService;

    private User owner;
    private User otherUser;
    private User admin;

    @BeforeEach
    void setUp() {
        companyService = new CompanyService(companyDao, jobDao);

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setRole(UserRole.USER);

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole(UserRole.USER);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);
    }

    private Company companyOwnedBy(User user) {
        Company company = new Company();
        company.setOwner(user);
        company.setName("Acme");
        return company;
    }

    @Test
    void createSetsOwnerFromAuthenticatedUserNotFromRequest() {
        CompanyRequest request = new CompanyRequest();
        request.setName("Acme");
        when(companyDao.saveCompany(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Company created = companyService.create(request, owner);

        assertThat(created.getOwner()).isEqualTo(owner);
        assertThat(created.getName()).isEqualTo("Acme");
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isNotNull();
    }

    @Test
    void createRejectsBlankName() {
        CompanyRequest request = new CompanyRequest();
        request.setName("   ");

        assertThatThrownBy(() -> companyService.create(request, owner))
                .isInstanceOf(JobsValidationException.class);

        verifyNoInteractions(companyDao);
    }

    @Test
    void ownerCanGetTheirOwnCompany() {
        UUID id = UUID.randomUUID();
        Company company = companyOwnedBy(owner);
        when(companyDao.getCompanyById(id)).thenReturn(Optional.of(company));

        assertThat(companyService.get(id, owner)).isEqualTo(company);
    }

    @Test
    void adminCanGetAnyCompany() {
        UUID id = UUID.randomUUID();
        Company company = companyOwnedBy(owner);
        when(companyDao.getCompanyById(id)).thenReturn(Optional.of(company));

        assertThat(companyService.get(id, admin)).isEqualTo(company);
    }

    @Test
    void otherUserCannotGetSomeoneElsesCompany() {
        UUID id = UUID.randomUUID();
        Company company = companyOwnedBy(owner);
        when(companyDao.getCompanyById(id)).thenReturn(Optional.of(company));

        assertThatThrownBy(() -> companyService.get(id, otherUser))
                .isInstanceOf(JobsAccessDeniedException.class);
    }

    @Test
    void getThrowsNotFoundForMissingCompany() {
        UUID id = UUID.randomUUID();
        when(companyDao.getCompanyById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.get(id, owner))
                .isInstanceOf(JobsNotFoundException.class);
    }

    @Test
    void listReturnsAllCompaniesForAdmin() {
        companyService.list(admin);
        verify(companyDao).getAllCompanies();
        verify(companyDao, never()).getAllCompaniesByOwner(any());
    }

    @Test
    void listReturnsOnlyOwnCompaniesForRegularUser() {
        companyService.list(owner);
        verify(companyDao).getAllCompaniesByOwner(owner);
        verify(companyDao, never()).getAllCompanies();
    }

    @Test
    void deleteBlockedWhenCompanyStillHasJobs() {
        UUID id = UUID.randomUUID();
        Company company = companyOwnedBy(owner);
        when(companyDao.getCompanyById(id)).thenReturn(Optional.of(company));
        when(jobDao.existsByCompanyId(any())).thenReturn(true);

        assertThatThrownBy(() -> companyService.delete(id, owner))
                .isInstanceOf(JobsValidationException.class);

        verify(companyDao, never()).deleteCompany(any());
    }

    @Test
    void deleteSucceedsWhenNoJobsReferenceCompany() {
        UUID id = UUID.randomUUID();
        Company company = companyOwnedBy(owner);
        when(companyDao.getCompanyById(id)).thenReturn(Optional.of(company));
        when(jobDao.existsByCompanyId(any())).thenReturn(false);

        companyService.delete(id, owner);

        verify(companyDao).deleteCompany(company.getId());
    }

    @Test
    void getOwnedByExactlyRejectsEvenWhenCheckingUserIsAdmin() {
        // getOwnedByExactly takes the *job's* owner, never the requester's role —
        // an admin ID passed in as "owner" must still fail if the company isn't theirs.
        UUID id = UUID.randomUUID();
        Company company = companyOwnedBy(owner);
        when(companyDao.getCompanyById(id)).thenReturn(Optional.of(company));

        assertThatThrownBy(() -> companyService.getOwnedByExactly(id, admin))
                .isInstanceOf(JobsAccessDeniedException.class);
    }

    @Test
    void getOwnedByExactlySucceedsForTheActualOwner() {
        UUID id = UUID.randomUUID();
        Company company = companyOwnedBy(owner);
        when(companyDao.getCompanyById(id)).thenReturn(Optional.of(company));

        assertThat(companyService.getOwnedByExactly(id, owner)).isEqualTo(company);
    }

    @Test
    void listCompaniesReturnsWhatDaoProvides() {
        when(companyDao.getAllCompaniesByOwner(owner)).thenReturn(List.of(companyOwnedBy(owner)));

        List<Company> result = companyService.list(owner);

        assertThat(result).hasSize(1);
    }
}
