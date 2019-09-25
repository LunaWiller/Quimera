package test.api.core;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.response.Response;
import test.api.core.TestEnvironment.AmbinetConfigs;
import test.api.core.TestEnvironment.DataBasesConfig;
import test.api.core.TestEnvironment.HttpConfigs;

/**	
 * Este é o coração da automação, com os métodos e propriedades principais.
 * <br>
 * O TestEngine tem como responsabilidade dar o Start inicial para os testes, capturar as informações padrões do TestEnvironmentConfigurationFile.Json e armazenar em uma propriedade do tipo TestEnvironment.
 * <br>
 * @see <a href="http://git.senior.com.br/gestao-empresarial/erpx-core-api-test/wikis/TestEngine#testengine">TestEngine</a>
**/
public class TestEngine {
	private static String jsonConfig = "TestEnvironmentConfigurationFile.Json";

	public static TestEnvironment environment;
	public static String urlApiDefault;
	public static List<String> relatorTest;
	public static String tituloTest;
	public static List<String> conteudoTest;

	/* [Rotinas Padroes do CORE] */

	/**	
	 * Buscar as informações do TestEnvironmentConfigurationFile.Json e popular a variável environment com os dados, umas das variáveis populadas é a urlApiDefault.
	 * <br>
	 * Ele popula a mesma com as informações do TestEnvironment.HttpConfigs para criar essa URL, a formula para a criação deste parâmetro é: Protocol + "://" + Host + ":" + Port + Patch + Version + TypeRequest.
	 * <br>
	 * @see <a href="http://git.senior.com.br/gestao-empresarial/erpx-core-api-test/wikis/TestEngine#m%C3%A9todo-getenvironmentdefaults">getEnvironmentDefaults</a>
	**/
	protected static void getEnvironmentDefaults() {
		environment = new TestEnvironment();
		TestEnvironment.HttpConfigs httpConfig = new HttpConfigs();
		TestEnvironment.AmbinetConfigs ambinetConfigs = new AmbinetConfigs();
		TestEnvironment.DataBasesConfig dataBasesConfig = new DataBasesConfig();
		ObjectMapper mapper = new ObjectMapper();
        try {
        	File jsonInputFile = new File(jsonConfig);
        	JsonNode noder = mapper.readTree(jsonInputFile);
        	httpConfig.setProtocol(noder.path("HttpConfigs").path("protocol").asText());
        	httpConfig.setHost(noder.path("HttpConfigs").path("host").asText());
        	httpConfig.setPort(noder.path("HttpConfigs").path("port").asText());
        	httpConfig.setPatch(noder.path("HttpConfigs").path("patch").asText());
        	httpConfig.setVersion(noder.path("HttpConfigs").path("version").asText());
    		httpConfig.setTypeRequest(noder.path("HttpConfigs").path("typeRequest").asText());
    		ambinetConfigs.setUsername(noder.path("ambinetConfigs").path("tenant").asText());
    		ambinetConfigs.setPassword(noder.path("ambinetConfigs").path("password").asText());
    		dataBasesConfig.setDatabaseType(noder.path("dataBasesConfig").path("databaseType").asText());
    		dataBasesConfig.setBase(noder.path("dataBasesConfig").path("base").asText());
    		dataBasesConfig.setHost(noder.path("dataBasesConfig").path("host").asText());
    		dataBasesConfig.setPort(noder.path("dataBasesConfig").path("port").asText());
    		dataBasesConfig.setSchemaName(noder.path("dataBasesConfig").path("schemaPrefix").asText());
    		dataBasesConfig.setUsuario(noder.path("dataBasesConfig").path("user").asText());
    		dataBasesConfig.setSenha(noder.path("dataBasesConfig").path("password").asText());
        }catch (Exception e) {
        	TestLogger.printLog("Erro ao tentar obter informação do ambiente de teste: " + e.getMessage());
		}
        environment.setAmbinetConfigs(ambinetConfigs);
        environment.setDataBasesConfig(dataBasesConfig);
        environment.setHttpConfigs(httpConfig);
        urlApiDefault = httpConfig.getProtocol() + "://" + httpConfig.getHost() + ":" + httpConfig.getPort() + httpConfig.getPatch() + httpConfig.getVersion() + httpConfig.getTypeRequest();
        TestLogger.printLog("urlApiDefault: " + urlApiDefault);
	}

	/**	
	 * Busca o link padrão para as requisições das apis.
	 * <br>
	 * @return Retorna o link formado pela fórmula: Protocol + "://" + Host + ":" + Port + Patch + Version + TypeRequest.
	 * <br>
	 * @see <a href="http://git.senior.com.br/gestao-empresarial/erpx-core-api-test/wikis/TestEngine#m%C3%A9todo-geturlapidefault">getUrlAPIDefault</a>
	**/
	protected static String getUrlAPIDefault() {
		getEnvironmentDefaults();
		return urlApiDefault;
	}


	/* [Rotinas Padroes do CORE] */

	/**	
	 * Transformar uma classe em um JSON.
	 * <br>
	 * <b>[Apenas usado no <a href="http://git.senior.com.br/gestao-empresarial/erpx-core-api-test/wikis/TestRequest">TestRequest</a>]</b>
	 * <br>
	 * @param body (Object) = Deverá ser uma classe, ou um objeto passível a ser parseado em JSON.
	 * <br>
	 * @return Retorna uma string com os dados informados no formato JSON.
	 * <br>
	 * @see <a href="http://git.senior.com.br/gestao-empresarial/erpx-core-api-test/wikis/TestEngine#m%C3%A9todo-geturlapidefault">encodeJsonBody</a>
	**/
	protected static String encodeJsonBody(Object body) {
		ObjectMapper mapper = new ObjectMapper();
		String bodyEncode = null;
		try {
			bodyEncode = mapper.writeValueAsString(body);
		} catch (JsonProcessingException e) {
			TestLogger.logInfo("Mensagem de Erro: " + e.getMessage());
		}
		return bodyEncode;
	}

	/**	
	 * Função para buscar o valor de um elementos Json dentro de um caminho específico.
	 * <br>
	 * @param response (Response) = Deverá receber o retorno de uma requisição do RestAssured.io.
	 * @param caminho (String) = Conter o caminho de elementos até o json especifico.
	 * <br>
	 * @return O valor encontrado para o elemento buscado.
	 * <br>
	 * @see <a href="http://git.senior.com.br/gestao-empresarial/erpx-core-api-test/wikis/TestEngine#m%C3%A9todo-getjsonvalueresponse-response-string-caminho">getJsonValue</a>
	**/
	protected static String getJsonValue(Response response, String caminho) {
		String valor = null;
		try {
			valor = response.body().jsonPath().get(caminho).toString();
		}catch (Exception e) {
			TestLogger.logInfo("Mensagem de Erro: " + e.getMessage());
		}
		return valor;
	}

	/**	
	 * Retornar a quantidade de elementos filhos de um elementos específico.
	 * <br>
	 * @param response (Response) = Deverá receber o retorno de uma requisição do RestAssured.io.
	 * @param caminho (String) = Conter o caminho de elementos até o json especifico.
	 * <br>
	 * @return Numero da quantidade de elementos filhos do elemento buscado.
	 * <br>
	 * @see <a href="http://git.senior.com.br/gestao-empresarial/erpx-core-api-test/wikis/TestEngine#m%C3%A9todo-getjsontotalelementsresponse-responsebody-string-caminho">getJsonTotalElements</a>
	**/
	protected static int getJsonTotalElements(Response responseBody, String caminho) {
		int size = 0;
		try {
			List<Map<String, String>> jsonObject = responseBody.getBody().jsonPath().getJsonObject(caminho);
			size = jsonObject.size();
		}catch (Exception e) {
			TestLogger.logInfo("Mensagem de Erro: " + e.getMessage());
		}
		return size;
	}
	
}