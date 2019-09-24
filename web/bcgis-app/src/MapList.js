import React from 'react';
import ReactDOM from 'react-dom';
import Map from './Map';

export class MapList extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            dataStore: JSON.parse(props.response).data.dataStore,
        };
    }

    showMap() {
        return (
            ReactDOM.render(<Map />, document.getElementById('map'))
        );
    }
    
    render() {
        return(
            // TODO 使用 Ant Design（https://ant.design/components/card-cn/）来显示所有已发布的图层
            <ul>
                <li>{this.state.dataStore.name}</li>
                <li>{this.state.dataStore.description}</li>
                <li>{this.state.dataStore.type}</li>
                <li>{this.state.ennabled ? "true" : "false"}</li>
                <li><a href="#" onClick={this.showMap}>{this.state.dataStore.name}:{this.state.dataStore.workspace.name}</a></li>
                {/* <li>{this.state.dataStore.workspace.name}</li>
                <li>{this.state.dataStore.workspace.href}</li> */}
            </ul>
        );
    }
}

export default MapList;

// Jersey 返回的示例数据： {"data":{"dataStore":{"name":"D","description":"D","type":"BCGIS","enabled":true,"workspace":{"name":"D","href":"http://localhost:8070/geoserver/rest/workspaces/D.json"},"connectionParameters":{"entry":[{"@key":"functionName","$":"GetRecordByKey"},{"@key":"recordKey","$":"6bff876faa82c51aee79068a68d4a814af8c304a0876a08c0e8fe16e5645fde4"},{"@key":"namespace","$":"D"},{"@key":"config","$":"file:network-config-test.yaml"},{"@key":"chaincodeName","$":"bcgiscc"}]},"_default":false,"featureTypes":"http://localhost:8070/geoserver/rest/workspaces/D/datastores/D/featuretypes.json"}},"status":200,"statusText":"OK","headers":{"content-type":"text/plain"},"config":{"url":"http://localhost:8899/bcgis/mapservice/wms/list","method":"get","headers":{"Accept":"application/json, text/plain, */*"},"transformRequest":[null],"transformResponse":[null],"timeout":0,"xsrfCookieName":"XSRF-TOKEN","xsrfHeaderName":"X-XSRF-TOKEN","maxContentLength":-1},"request":{}}
