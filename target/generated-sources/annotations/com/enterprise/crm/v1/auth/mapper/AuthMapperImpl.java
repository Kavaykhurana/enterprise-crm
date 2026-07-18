package com.enterprise.crm.v1.auth.mapper;

import com.enterprise.crm.v1.auth.dto.AuthResponse;
import com.enterprise.crm.v1.auth.dto.RegisterRequest;
import com.enterprise.crm.v1.user.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T14:03:30+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.1 (Oracle Corporation)"
)
@Component
public class AuthMapperImpl implements AuthMapper {

    @Override
    public User registerRequestToUser(RegisterRequest request) {
        if ( request == null ) {
            return null;
        }

        User user = new User();

        user.setEmail( request.getEmail() );
        user.setFirstName( request.getFirstName() );
        user.setLastName( request.getLastName() );
        user.setPhoneNumber( request.getPhoneNumber() );
        user.setProfileImageUrl( request.getProfileImageUrl() );
        user.setRole( request.getRole() );

        return user;
    }

    @Override
    public AuthResponse userToAuthResponse(User user) {
        if ( user == null ) {
            return null;
        }

        AuthResponse.AuthResponseBuilder authResponse = AuthResponse.builder();

        authResponse.userId( user.getId() );
        authResponse.email( user.getEmail() );
        authResponse.role( user.getRole() );

        return authResponse.build();
    }
}
