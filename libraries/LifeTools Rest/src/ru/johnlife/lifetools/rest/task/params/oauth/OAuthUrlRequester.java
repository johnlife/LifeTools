package ru.johnlife.lifetools.rest.task.params.oauth;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.SignatureType;
import com.github.scribejava.core.model.Verb;

import java.util.Map;

import ru.johnlife.lifetools.rest.task.params.UrlRequester;

/**
 * Created by yanyu on 5/5/2016.
 */
public class OAuthUrlRequester extends UrlRequester {
    private String initialUrl;
    private OAuthSigner signer;

    public OAuthUrlRequester(String url, Object[][] params, OAuthSigner signer) {
        super(url, params);
        initialUrl = url;
        this.signer = signer;
    }

    @Override
    protected Object[][] getParams() {
        if (getSignatureType() == SignatureType.QueryString) {
            OAuthRequest oReq = getOAuthRequest();
            Map<String, String> oauthParameters = oReq.getOauthParameters();
            int size = oauthParameters.size();
            Object[][] result = new Object[size][];
            int i=0;
            for (Map.Entry<String, String> entry : oauthParameters.entrySet()) {
                result[i++] = new String[]{entry.getKey(), entry.getValue()};
            }
            return result;
        } else {
            return super.getParams();
        }
    }

    private SignatureType getSignatureType() {
        return signer.getService().getConfig().getSignatureType();
    }

    private OAuthRequest getOAuthRequest() {
        Object[][] params = super.getParams();
        OAuthRequest oReq = new OAuthRequest(Verb.GET, initialUrl, signer.getService());
        for (Object[] param : params) {
            oReq.addQuerystringParameter(param[0].toString(), param[1].toString());
        }
        return signer.signRequest(oReq);
    }

    @Override
    public Map<String, String> getHeaders() {
        if (getSignatureType() == SignatureType.Header) {
            return getOAuthRequest().getHeaders();
        } else {
            return super.getHeaders();
        }
    }
}
