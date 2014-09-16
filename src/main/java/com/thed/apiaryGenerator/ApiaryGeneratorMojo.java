package com.thed.apiaryGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.thed.model.api.Operation;
import com.thed.model.api.QueryParameter;
import com.thed.model.api.Resource;
import com.thed.model.api.Util;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Generates apiary documentation
 *
 */
@Mojo(requiresDependencyResolution = ResolutionScope.RUNTIME, name = "generateApiDocs")
 
public class ApiaryGeneratorMojo extends AbstractMojo {
    Logger logger = Logger.getLogger(ApiaryGeneratorMojo.class.getName());
    @Parameter
//    private String packageName;
    private String packageName="com.thed.service.rest.resource";
    @Parameter
    private String vmFile = "apiary.vm";

    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @Parameter
//    private String outputFileName;
    private String outputFileName="target/apiary.apib";

    public void execute() throws MojoExecutionException, MojoFailureException {
    	Collection<Class> sortedTypes =null; //getResourceClasses();
		try {
			sortedTypes = new Util().getClasses(packageName);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//getResourceClasses();
        List<Resource> list = new ArrayList<Resource>();
        for(Class type : sortedTypes){
            if(type.getName().startsWith(packageName) && type.isInterface()){
                System.out.println(type);
                try {
					list.add(getResourceMetadata(type));
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
        generateDocs(list);
    }      

	private List<Class<?>> getResourceClasses() {		
		Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forJavaClassPath())
        );
        Set<Class<?>> types = reflections.getTypesAnnotatedWith(Path.class);
        Ordering<Class> order = new Ordering<Class>() {
            @Override
            public int compare(Class left, Class right) {
                String leftName = StringUtils.substringAfterLast(left.getName(), ".");
                String rightName = StringUtils.substringAfterLast(right.getName(), ".");
                return leftName.compareTo(rightName);
            }
        };
        List<Class<?>> sortedTypes = order.sortedCopy(Iterables.filter(types, new Predicate<Class<?>>() {
            public boolean apply(Class<?> input) {
                return input.getName().startsWith(packageName);
            }
        }));
		return sortedTypes;
	}
    

	/* Sample annotations -
	@Service("zephyrTestcase")
	@Path("/testcase")
	@Produces({MediaType.APPLICATION_JSON , MediaType.APPLICATION_XML})
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Api(value = "/testcase", description = "get testcase by id and criteria")
	*/
	private Resource getResourceMetadata(Class clazz) throws IOException {
		Resource r = new Resource();

		Annotation[] ann = clazz.getAnnotations() ;

		Service service = (Service)clazz.getAnnotation(Service.class);
//		Service s = (Service) service ;
		Path path = (Path) clazz.getAnnotation(Path.class);
		Produces produces = (Produces) clazz.getAnnotation(Produces.class);
		Consumes consumes = (Consumes) clazz.getAnnotation(Consumes.class);
		Api api = (Api) clazz.getAnnotation(Api.class);

		r.setName(extractResourcePrefix(clazz.getName()));
		r.setGroupNotes(api.description());
		r.setPath(supressDuplicateSlash(path.value()));
		r.setProduces(StringUtils.join(produces.value(), " "));
		r.setConsumes(StringUtils.join(consumes.value(), " "));

		for (Method m: clazz.getMethods())  {
			getOperationMetadata(r, m);
		}
		return r ;
	}

	private String extractResourcePrefix(String s) {
		String[] sa = StringUtils.split(s, ".");
		String resourceName = sa[sa.length-1];
		String name = resourceName.substring(0, resourceName.indexOf("Resource"));
		return name ;
	}

	/*
	@GET
	@Path("/{id}")
	@ApiOperation(value = "Get testcase by ID", //notes = "Add extra notes here",
					responseClass = "com.thed.rpc.bean.RemoteRepositoryTreeTestcase")
	@ApiErrors(value = { @ApiError(code = 400, reason = "Invalid ID supplied"),
							@ApiError(code = 404, reason = "Testcase not found") })
	*/
	private void getOperationMetadata(Resource r, Method m) throws IOException {
		Operation op = new Operation();
		if (m.getAnnotation(GET.class) != null) {
			op.setRequestType("GET");
		} else if (m.getAnnotation(POST.class) != null) {
			op.setRequestType("POST");
		} else if (m.getAnnotation(PUT.class) != null) {
			op.setRequestType("PUT");
		} else if (m.getAnnotation(DELETE.class) != null) {
			op.setRequestType("DELETE");
		}

		Path path = (Path) m.getAnnotation(Path.class);
		op.setPath(supressDuplicateSlash(r.getPath() + "/" + path.value()));

		ApiOperation api = (ApiOperation) m.getAnnotation(ApiOperation.class);
		if (api != null) {
			op.setName(api.value());
			op.setDescription(api.value());	// don't have description, duplicating name value
		} else {
			op.setName("TODO: please add description");
			op.setDescription("TODO: please add description");
		}

		// use Resource's annotation if required
		if (m.getAnnotation(Produces.class) != null) {
			Produces produces = (Produces) m.getAnnotation(Produces.class);
			op.setProduces(StringUtils.join(produces.value(), " "));
		} else {
			op.setProduces(r.getProduces());
		}

		if (m.getAnnotation(Consumes.class) != null) {
			Consumes consumes = (Consumes) m.getAnnotation(Consumes.class);
			op.setConsumes(StringUtils.join(consumes.value(), " "));
		} else {
			op.setConsumes(r.getConsumes());
		}

		if (r.getOperations() == null) {
			r.setOperations(new ArrayList<Operation>());
		}
		r.getOperations().add(op);		
		op.setJsonRequest(getRequestResponse(r,m,op,new String("request")));
		op.setJsonResponse(getRequestResponse(r,m,op,new String("response")));
		op.setResponseCode("200");

		getUrlParameter(r, op, m);
	}

	// Get the requst/response fronm a text file on a particular location src/main/resources/apidocs/<resource>/<method>/<request>/response.json e.g. src/main/resources/apidocs/attachment/getAttachment/GET/response.json
	private List<String> getRequestResponse(Resource r, Method m, Operation op, String exampleString) throws IOException {
		String file = null;
		FileInputStream fstream;

		
		if (exampleString.equals("request")) {
			if (m.getName().equals("getManifest")) {
				file = "src/main/resources/apidocs/" + "manifest" + "/" +  m.getName() + "/" + op.getRequestType() +"/" + "request.json";
			} else {
				file = "src/main/resources/apidocs/" + r.getPath() + "/" +  m.getName() + "/" + op.getRequestType() +"/" + "request.json";
			}
		}else {
			if (m.getName().equals("getManifest")) {
				file = "src/main/resources/apidocs/" + "manifest" + "/" +  m.getName() + "/" + op.getRequestType() +"/" + "response.json";
			} else {
				file = "src/main/resources/apidocs/" + r.getPath() + "/" +  m.getName() + "/" + op.getRequestType() +"/" + "response.json";
			}
		}

		fstream = new FileInputStream(file);
		List<String> list = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String line;
		while ((line = br.readLine()) != null) {
			list.add(line);
		}
		br.close();
		fstream.close();
		return list;
	}	

	private void getUrlParameter(Resource r, Operation op, Method m) {
		Annotation[][] pa = m.getParameterAnnotations() ;
//		System.out.println(pa);

		/* E.g. AttachmentResource  */
		/*
		public List<RemoteAttachment> getAttachments(
				@ApiParam(value = "Id of entity which need to be fetched", required = true)
				@QueryParam("entityid") String entityId,
				@ApiParam(value = "Entity name, possible values : testcase, requirement, testStepResult, releaseTestSchedule")
				@QueryParam("entityname") String entityName,
				@ApiParam(value = "Token stored in cookie, fetched automatically if available", required = false)
				@CookieParam("token") Cookie tokenFromCookie) throws ZephyrServiceException;
		*/
		Class[] params = m.getParameterTypes() ;
//		TypeVariable<Method>[] tvm = m.getTypeParameters();
		for (int i = 0; i < pa.length; i++) {
			Annotation[] eachParam = pa[i] ;
			// ignore ApiParam or PathParam or CookieParam ignore
			QueryParam qpAnnotation = hasQueryParam(eachParam) ;
			if (qpAnnotation != null) {

				if (op.getQueryParams() == null) {
					List<QueryParameter> queryParams = new ArrayList<QueryParameter>();
					op.setQueryParams(queryParams);
				}
				
				QueryParameter qParam = new QueryParameter();
				qParam.setName(qpAnnotation.value());
				qParam.setType(params[i].getSimpleName());
				qParam.setDescription(getApiDescription(eachParam));
				op.getQueryParams().add(qParam);
			}
		}
		
	}
	
	private QueryParam hasQueryParam(Annotation[] paramAnnotaions) {
		for (Annotation ax: paramAnnotaions) {
			if (ax instanceof QueryParam) {
				return (QueryParam) ax ;
			}
		}
		return null ;
	}
	
	private String getApiDescription(Annotation[] paramAnnotaions) {
		for (Annotation ax: paramAnnotaions) {
			if (ax instanceof ApiParam) {
				return ((ApiParam) ax).value() ;
			}
		}
		return "TODO: please provide a description" ;
	}
	
	private String supressDuplicateSlash(String s) {
		String rep = s.replaceAll("//", "/");
		return rep ;
	}
	

    /**
     *
     * @param resources
     */
	private void generateDocs(List<Resource> resources) {
		Velocity.init();
		VelocityContext context = new VelocityContext();
		context.put("name", new String("Velocity"));
		Template template = null;
		
		context.put("resources", resources);
		context.put("DOUBLE_HASH", "##");
		context.put("TRIPLE_HASH", "###");
        PrintWriter pw = null;
		try {
            VelocityEngine ve = new VelocityEngine();
            ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
			template = ve.getTemplate(vmFile);
			StringWriter sw = new StringWriter();
			template.merge(context, sw);
			pw = new PrintWriter(new File(outputFileName));
            pw.write(sw.toString());
            pw.flush();
            logger.fine("Log file is generated " + outputFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
            pw.close();
        }
	}	
	
	 public String getPackageName() {
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		public String getVmFile() {
			return vmFile;
		}

		public void setVmFile(String vmFile) {
			this.vmFile = vmFile;
		}

		public String getOutputFileName() {
			return outputFileName;
		}

		public void setOutputFileName(String outputFileName) {
			this.outputFileName = outputFileName;
		}

}