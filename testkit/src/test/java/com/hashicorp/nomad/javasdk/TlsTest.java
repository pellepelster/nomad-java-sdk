package com.hashicorp.nomad.javasdk;

import com.hashicorp.nomad.testutils.NomadAgentConfiguration;
import com.hashicorp.nomad.testutils.TestAgent;
import org.junit.Test;

public class TlsTest extends ApiTestBase {

    private String certPath(String filename) {
        return "src/test/resources/com/hashicorp/nomad/testutils/" + filename;
    }

    @Test
    public void shouldUseClientCertificateAgainstServer() throws Exception {
        try (TestAgent agent = newAgent(
                new NomadAgentConfiguration.Builder()
                        .setTlsHttp(true)
                        .setTlsCaFile(certPath("nomad-ca.pem"))
                        .setTlsCertAndKeyFiles(certPath("server.pem"), certPath("server-key.pem")),
                new NomadApiConfiguration.Builder()
                        .setTlsCaFile(certPath("nomad-ca.pem"))
                        .setTlsCertAndKeyFiles(certPath("sdk.pem"), certPath("sdk-key.pem"))
        )) {
            agent.getApiClient().getAgentApi().self();
        }
    }

    @Test
    public void shouldUseClientCertificateAgainstClient() throws Exception {
        NomadAgentConfiguration.Builder agentConfigBuilder = new NomadAgentConfiguration.Builder()
                .setTlsHttp(true)
                .setTlsCaFile(certPath("nomad-ca.pem"))
                .setTlsCertAndKeyFiles(certPath("client.pem"), certPath("client-key.pem"));
        NomadApiConfiguration.Builder apiConfigBuilder = new NomadApiConfiguration.Builder()
                .setTlsCaFile(certPath("nomad-ca.pem"))
                .setTlsCertAndKeyFiles(certPath("sdk.pem"), certPath("sdk-key.pem"));

        try (TestAgent server = newAgent(agentConfigBuilder, apiConfigBuilder)) {
            try (TestAgent client = newAgent(
                    agentConfigBuilder
                            .setServerEnabled(false)
                            .setServerBootstrapExpect(0)
                            .setClientEnabled(true)
                    .setServerStartJoin(),
                    apiConfigBuilder)) {
                client.getApiClient().getAgentApi().setServers(server.getRpcAddress());
            }
        }
    }

    @Test
    public void shouldUsePKCS8ClientCertificateAgainstClient() throws Exception {
        NomadAgentConfiguration.Builder agentConfigBuilder = new NomadAgentConfiguration.Builder()
                .setTlsHttp(true)
                .setTlsCaFile(certPath("nomad-ca.pem"))
                .setTlsCertAndKeyFiles(certPath("client.pem"), certPath("client-key.pem"));
        NomadApiConfiguration.Builder apiConfigBuilder = new NomadApiConfiguration.Builder()
                .setTlsCaFile(certPath("nomad-ca.pem"))
                .setTlsCertAndKeyFiles(certPath("sdk.pem"), certPath("sdk-key-p8.pem"));

        try (TestAgent server = newAgent(agentConfigBuilder, apiConfigBuilder)) {
            try (TestAgent client = newAgent(
                    agentConfigBuilder
                            .setServerEnabled(false)
                            .setServerBootstrapExpect(0)
                            .setClientEnabled(true)
                            .setServerStartJoin(),
                    apiConfigBuilder)) {
                client.getApiClient().getAgentApi().setServers(server.getRpcAddress());
            }
        }
    }

}
