package com.mobigen.iris.jdbc;

/**
 * IRISResultSet.java
 * <p>
 * <p>
 * HISTORY
 * 코드 변경 이력 정보를 남긴다.
 * - 2009-05-12 : IRISResultSet class 생성
 * - 2014-04-02 : 불필요한 함수 외부 클래스(IRISUnused)로 분리
 *
 * @author sungwon Park
 * @author Woo-Cheol Kim
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class IRISVirtualResultSet extends IRISResultSet implements ResultSet {

    IRISVirtualResultSet(Statement creator) throws SQLException {
        super(creator);
    }
}
