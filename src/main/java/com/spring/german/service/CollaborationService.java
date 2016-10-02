package com.spring.german.service;

import com.spring.german.entity.Project;
import com.spring.german.entity.Technology;
import com.spring.german.entity.User;
import com.spring.german.exceptions.CustomException;
import com.spring.german.repository.ProjectRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CollaborationService {

    private UserService userService;
    private ProjectRepository projectRepository;

    @Autowired
    public CollaborationService(UserService userService,
                                ProjectRepository projectRepository) {
        this.userService = userService;
        this.projectRepository = projectRepository;
    }

    private static final String REGEX = "\\[([a-zA-z ]*)\\]\\(.+\\)";

    private static final Logger log = LoggerFactory.getLogger(CollaborationService.class);

    public List<String> getTechnologies(String username, String repoName) {

        String body;
        try {
            URL url = new URL("https://raw.githubusercontent.com/" + username + "/" + repoName + "/master/README.md");
            log.info("Application will parse readme from the following url: {}", url.toString());
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            body = IOUtils.toString(in, encoding);
        } catch (IOException e) {
            throw new CustomException("There is no such user on github, or " +
                    "repository name you've specified is non existent", e);
        }
        List<String> technologies = new ArrayList<>();
        Matcher m = Pattern.compile(REGEX).matcher(body);
        while (m.find()) {
            technologies.add(m.group(1));
        }
        return technologies;
    }

    public void saveProjectWithTechnologies(String username, List<String> technologies) {

        User user = userService.findBySso(username);
        log.info("User fetched by username (in CollaborationService): {}", user);

        Project project = new Project("default", user);

        List<Technology> technologiesToSave = technologies.stream()
                .map(t -> new Technology(t, project)).collect(Collectors.toList());

        project.setTechnologies(technologiesToSave);

        projectRepository.save(project);
    }
}
