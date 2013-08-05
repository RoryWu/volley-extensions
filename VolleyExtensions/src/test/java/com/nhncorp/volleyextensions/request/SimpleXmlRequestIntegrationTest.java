package com.nhncorp.volleyextensions.request;

import static com.jayway.awaitility.Awaitility.*;
import static com.nhncorp.volleyextensions.mock.ListenerVerifier.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.core.ElementException;

import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;
import com.github.kristofa.test.http.MockHttpServer;
import com.github.kristofa.test.http.SimpleHttpResponseProvider;
import com.nhncorp.volleyextensions.mock.ErrorResponseHoldListener;
import com.nhncorp.volleyextensions.mock.MockExecutorDelivery;
import com.nhncorp.volleyextensions.mock.ResponseHoldListener;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SimpleXmlRequestIntegrationTest {
	private static final int THREAD_POOL_SIZE = 4;
	private static final int PORT = 51234;
	private static final String url = "http://localhost:" + PORT;
	private RequestQueue requestQueue;
	private SimpleHttpResponseProvider responseProvider;
	private MockHttpServer server;
	ResponseHoldListener<Person> listener = new ResponseHoldListener<Person>();
	ErrorResponseHoldListener errorListener = new ErrorResponseHoldListener();

	@BeforeClass
	public static void setUpOnce() throws Exception {
		ShadowLog.stream = System.out;
	}

	@Before
	public void setUp() throws Exception {
		// init a Volley RequestQueue
		requestQueue = new RequestQueue(new NoCache(), new BasicNetwork(
				new HurlStack()), THREAD_POOL_SIZE, new MockExecutorDelivery());
		requestQueue.start();
		// init mock http server
		responseProvider = new SimpleHttpResponseProvider();
		server = new MockHttpServer(PORT, responseProvider);
		server.start();
	}

	@After
	public void tearDown() throws Exception {
		// stop the mock http server of running
		server.stop();

		// stop the volley
		requestQueue.stop();
	}

	@Test
	public void responseShouldBeParsed() throws Exception {
		// Given
		responseProvider.expect(com.github.kristofa.test.http.Method.GET, "/")
				.respondWith(200, "application/xml",
						"<xml><name>hello</name></xml>");

		// When
		SimpleXmlRequest<Person> request = new SimpleXmlRequest<Person>(
				url, Person.class, listener, errorListener);
		requestQueue.add(request);

		// Then
		with().await("testValidJson").until(wasListenerCalled(listener));
		Person person = listener.getLastResponse();
		assertThat(person.name, is("hello"));
	}

	@Test
	public void errorShouldBeListened() throws Exception {
		// Given
		responseProvider.expect(com.github.kristofa.test.http.Method.GET, "/")
				.respondWith(200, "application/json",
						"<xml><name>hello</name><xml>");

		// When
		SimpleXmlRequest<Person> request = new SimpleXmlRequest<Person>(
				url, Person.class, listener, errorListener);
		requestQueue.add(request);

		// Then
		with().await("testInvalidJson").until(
				wasErrorListenerCalled(errorListener));
		VolleyError error = errorListener.getLastError();
		assertThat(error, is(instanceOf(ParseError.class)));
		assertThat(error.getCause(), is(instanceOf(ElementException.class)));		
	}

	/** just for test */
	@org.simpleframework.xml.Root(name = "xml")
	private static class Person {
		@Element(name = "name")
		public String name;
	}
}
