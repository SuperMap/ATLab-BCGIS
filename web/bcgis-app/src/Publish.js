import React from 'react';

class Publish extends React.Component {
    render() {
        return (
            <form id="publish_form">
                工作空间名称： <input id="publish_workspace"></input> <br/>
                数据存储名称： <input id="publish_datastore"></input> <br/>
                图层名称： <input id="publish_layer"></input> <br/>
                <button>发布</button>
            </form>
        );
    }
}

export default Publish;