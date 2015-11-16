package ie.brianhenry.kyocerajobs;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PrinterTest {

	Printer service;

	@Before
	public void setUp() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		service = new Printer("87.35.237.21");

	}

	@Test
	public void loginTest() throws ClientProtocolException, IOException {
		String username = "Admin";
		String password = "Admin";
		CloseableHttpResponse response = service.login(username, password);

		assertEquals(200, response.getStatusLine().getStatusCode());

	}

	@Test
	public void getJobTest() throws ClientProtocolException, IOException {

		JobDetail j = service.getJob(1635);

		assertEquals(1, j.getJobType());
		assertEquals("doc00163520151014145654", j.getJobName());
		// assertEquals("", j.getUserName());
		assertEquals("connectedto", j.getConnectedTo());
		assertEquals(LocalDateTime.parse("2015/10/14 14:56", DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")),
				j.getAcceptedTime());
		assertEquals(LocalDateTime.parse("2015/10/14 14:57", DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")),
				j.getEndTime());
		assertEquals(1, j.getOriginalPages());
		assertEquals(1, j.getCopies());
		assertEquals(1, j.getPrintedPages());

	}

	@Test
	public void getRecentJobsTest() throws ClientProtocolException, IOException {
		int pageNumber = 1;
		JobDetail[] jobs = service.getRecentJobs(pageNumber);
		assertEquals(10, jobs.length);
	}

	@Test
	public void parseJobsPageShortTest() throws IOException {

		String shortString = "JobKey[Index] = \"11\"; sdasdfsadf JobKey[Index] = \"12\"; asfdasdf asd JobKey[Index] = \"13\"; asdfsad fJobKey[Index] = \"14\"; asdfasdfasdfasdfsad fJobKey[Index] = \"15\"; sdasdfsadf JobKey[Index] = \"16\"; asfdasdf asd JobKey[Index] = \"17\"; asdfsad fJobKey[Index] = \"18\"; asdfasdfasdfasdfsad fJobKey[Index] = \"19\"; asdfasdfasdfJobKeyasdfasdfasdfJobKey[Index] = \"20\"; ";

		int[] shortNumbers = { 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };

		int[] shortJobs = service.parseJobsListHtml(shortString);

		Assert.assertArrayEquals(shortNumbers, shortJobs);

	}

	@Test
	public void parseJobsPageFullTest() throws IOException {

		String jobListHtml = IOUtils.toString(this.getClass().getResourceAsStream("jobs.html"), "UTF-8");

		int[] jobNumbers = { 2885, 2884, 2883, 2882, 2881, 2880, 2879, 2878, 2877, 2876 };

		int[] parsedJobs = service.parseJobsListHtml(jobListHtml);

		Assert.assertArrayEquals(jobNumbers, parsedJobs);

	}

	// TODO finish, move to own class + use!
	// Based on https://gist.github.com/helmbold/c7808b17bcf5a5d009cf
	abstract class CarelessHttpClient extends CloseableHttpClient {

		public CarelessHttpClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

			TrustStrategy trustStrategy = new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) {
					return true;
				}
			};

			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, trustStrategy).build();
			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
					new NoopHostnameVerifier());
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
					.register("http", PlainConnectionSocketFactory.getSocketFactory())
					.register("https", sslSocketFactory).build();

			PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
					socketFactoryRegistry);
			HttpClients.custom().setSSLContext(sslContext).setConnectionManager(connectionManager).build();

		}

	}

}