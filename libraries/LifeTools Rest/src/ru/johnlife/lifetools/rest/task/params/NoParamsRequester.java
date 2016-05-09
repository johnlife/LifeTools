package ru.johnlife.lifetools.rest.task.params;

/**
 * Created by yanyu on 4/24/2016.
 */
public class NoParamsRequester extends Requester {
    public NoParamsRequester(int method, String url) {
        super(method, url, null);
    }

    @Override
    public String getBody() {
        return null;
    }
}
