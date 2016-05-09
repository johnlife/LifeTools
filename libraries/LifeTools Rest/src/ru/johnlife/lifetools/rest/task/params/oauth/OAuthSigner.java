package ru.johnlife.lifetools.rest.task.params.oauth;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * Created by yanyu on 5/5/2016.
 */
public class OAuthSigner {
    private interface InternalSigner {
        OAuthRequest signRequest(OAuthRequest request);
    }

    private static class V1Signer implements InternalSigner {
        private OAuth10aService service;
        private OAuth1AccessToken token;

        public V1Signer(OAuth10aService service, OAuth1AccessToken token) {
            this.service = service;
            this.token = token;
        }

        @Override
        public OAuthRequest signRequest(OAuthRequest request) {
            service.signRequest(token, request);
            return request;
        }
    }
    private static class V2Signer implements InternalSigner {
        private OAuth20Service service;
        private OAuth2AccessToken token;

        public V2Signer(OAuth20Service service, OAuth2AccessToken token) {
            this.service = service;
            this.token = token;
        }

        @Override
        public OAuthRequest signRequest(OAuthRequest request) {
            service.signRequest(token, request);
            return request;
        }
    }

    private InternalSigner iSigner;
    private OAuthService service;



    public OAuthSigner(OAuth10aService service, OAuth1AccessToken token) {
        this.service = service;
        this.iSigner = new V1Signer(service, token);
    }

    public OAuthSigner(OAuth20Service service, OAuth2AccessToken token) {
        this.service = service;
        this.iSigner = new V2Signer(service, token);
    }

    public OAuthRequest signRequest(OAuthRequest request) {
        return iSigner.signRequest(request);
    }

    public OAuthService getService() {
        return service;
    }


}
