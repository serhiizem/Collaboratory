package com.spring.german.repository;

import com.spring.german.entity.UserProfile;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserProfileRepositoryTest {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    public void shouldFindAllUserProfilesWithSpecificAuthority() {

        UserProfile userProfile = userProfileRepository.findByType("USER");

        Assert.assertThat(userProfile.getType(), Matchers.is("USER"));
    }
}