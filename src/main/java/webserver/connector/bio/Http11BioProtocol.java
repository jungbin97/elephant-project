package webserver.connector.bio;

import webserver.connector.protocol.AbstractProtocol;

/**
 * {@code Http11BioProtocol}은 HTTP/1.1 프로토콜을 처리하기 위한
 * BIO 기반 프로토콜 구현입니다.
 *  <p>
 *    BioEndpoint 생성 및 초기화, 시작 및 중지 작업을 수행합니다.
 *  </p>
 *
 */
public class Http11BioProtocol extends AbstractProtocol {

    public Http11BioProtocol(int port) {
        super(port);
        this.endpoint = new BioEndpoint();
    }

    @Override
    protected void initInternal() throws Exception {

    }

    @Override
    protected void startInternal() throws Exception {

    }

    @Override
    protected void stopInternal() throws Exception {

    }

    @Override
    protected void destroyInternal() throws Exception {

    }
}
