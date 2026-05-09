package com.smartlogix.auth_service.client;
import com.smartlogix.auth_service.dto.CompanyDTO;
import com.smartlogix.auth_service.dto.MessageResponse;
import com.smartlogix.auth_service.dto.UserProfileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
@FeignClient(name = "ms-users")
public interface UsersClient {
    @PostMapping("/smartlogix/users/companies")
    MessageResponse<CompanyDTO> createCompany(@RequestBody CompanyDTO companyDTO);
    
    @PostMapping("/smartlogix/users/profiles/company/{companyId}/admin")
    MessageResponse<UserProfileDTO> createAdminProfile(@PathVariable("companyId") String companyId, @RequestBody UserProfileDTO profileDTO);
}
