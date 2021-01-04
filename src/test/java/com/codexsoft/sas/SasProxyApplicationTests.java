package com.codexsoft.sas;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SasProxyApplication.class)
@WebAppConfiguration
public class SasProxyApplicationTests {


	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(),
			Charset.forName("utf8"));

	private MockMvc mockMvc;

	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void setup() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).build();
	}

	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {

		this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
				.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
				.findAny()
				.orElse(null);

		assertNotNull("the JSON message converter must not be null",
				this.mappingJackson2HttpMessageConverter);
	}

	@Test
	public void testSASroot() throws Exception {
		mockMvc.perform(get(new URI("/sas/")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType));

	}

	@Test
	public void testSASServerNames() throws Exception {
		mockMvc.perform(get(new URI("/sas/servers/SASApp/")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType));
/*
*     .andExpect(jsonPath("$.id", is(this.bookmarkList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.uri", is("http://bookmark.com/1/" + userName)))
                .andExpect(jsonPath("$.description", is("A description")));
* */
	}

	@Test
	public void testGetLibrariesByServerConnection() throws Exception {
		mockMvc.perform(get(new URI("/sas/libraries/?serverUrl=sas.analytium.co.uk&serverPort=8591")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType));
	}

	@Test
	public void testGetLibrariesByServerName() throws Exception {
		mockMvc.perform(get(new URI("/sas/servers/SASApp/libraries/")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType));
	}

	@Test
	public void testGetLibraryByServerConnection() throws Exception {
		mockMvc.perform(get(new URI("/sas/libraries/SASHELP/?serverUrl=sas.analytium.co.uk&serverPort=8591")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType));
	}

	@Test
	public void testGetLibraryByServerName() throws Exception {
		mockMvc.perform(get(new URI("/sas/servers/SASApp/libraries/SASHELP/")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType));
	}

	@Test
	public void testGetDatasetsByServerConnection() throws Exception {
		mockMvc.perform(get(new URI("/sas/libraries/SASHELP/datasets/?serverUrl=sas.analytium.co.uk&serverPort=8591")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType));
	}

	@Test
	public void testGetDatasetsByServerName() throws Exception {
		mockMvc.perform(get(new URI("/sas/servers/SASApp/libraries/SASHELP/datasets/")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType));
	}

	@Test
	public void testGetDatasetByServerConnection() throws Exception {
		mockMvc.perform(get(new URI("/sas/libraries/SASHELP/datasets/CLASS/?serverUrl=sas.analytium.co.uk&serverPort=8591")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType));
	}

    @Test
    public void testGetDatasetByServerName() throws Exception {
        mockMvc.perform(get(new URI("/sas/servers/SASApp/libraries/SASHELP/datasets/CLASS/")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType));
    }

    @Test
    public void testGetDatasetDataByServerConnection() throws Exception {
        mockMvc.perform(get(new URI("/sas/libraries/SASHELP/datasets/CLASS/data/?serverUrl=sas.analytium.co.uk&serverPort=8591")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType));
    }

    @Test
    public void testGetDatasetDataByServerName() throws Exception {
        mockMvc.perform(get(new URI("/sas/servers/SASApp/libraries/SASHELP/datasets/CLASS/data/")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType));
    }

	@Test
	public void testGetDatasetDataByServerNameWithFilter() throws Exception {
		mockMvc.perform(get(new URI("/sas/servers/SASApp/libraries/SASHELP/datasets/CLASS/data?filter=%7B%22Sex%22%3A%20%22M%22%2C%20%22Age%22%3A%2014%7D")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType));
	}

    @Test
    public void testPutDatasetData() throws Exception {
        mockMvc.perform(put(new URI("/sas/servers/SASApp/libraries/MTEST/datasets/CLASS/data/"))
                    .content("[{\"Name\": \"Eugene\", \"Age\": 22}]")
                    .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType));
    }

	@Test
	public void testGetStpServers() throws Exception {
		mockMvc.perform(get(new URI("/sas/stp?repositoryName=Foundation")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType));
	}

	@Test
	public void testGetStpServerByName() throws Exception {
		mockMvc.perform(get(new URI("/sas/stp/SASApp?repositoryName=Foundation")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType));
	}

	@Test
	public void testGetUser() throws Exception {
		mockMvc.perform(get(new URI("/sas/user?repositoryName=Foundation")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType));
	}

    @Test
    public void testPostLibrary() throws Exception {
        mockMvc.perform(post(new URI("/sas/servers/SASApp/libraries/MTEST?repositoryName=Foundation&engine=V9&displayName=test%20library&path=%2Fpub%2Fcodex%2Fdata%2F&location=%2FUser%20Folders%2Fsasdev%2FMy%20Folder%2F&isPreassigned=true")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType));
    }

    @Test
    public void testPutCmd() throws Exception {
        mockMvc.perform(put(new URI("/sas/servers/SASApp/cmd?repositoryName=Foundation&logEnabled=true"))
                    .content("data a;x=1;run;proc print;run;")
                    .contentType("text/plain")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType));
    }

	@Test
	public void contextLoads() {
		assertNotNull("test");
	}

}
