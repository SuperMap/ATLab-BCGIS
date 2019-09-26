import React from 'react';
import * as ol from 'ol';
import TileLayer from 'ol/layer/Tile';
import TileWms from 'ol/source/TileWMS';
import { fromLonLat } from 'ol/proj';

export class Map extends React.Component {
    componentDidMount() {
        new ol.Map({
            target: 'mapdiv',
            layers: [
                new TileLayer({
                    source: new TileWms({
                        url: 'http://localhost:8070/geoserver/testWS/wms',
                        params: {
                            'LAYERS': 'testDS:testFT',
                            'TILED': true
                        },
                        serverType: 'geoserver',
                        transition: 0
                    })
                })
            ],
            view: new ol.View({
                center: fromLonLat([116.5, 40.18]),
                zoom: 9
            })
        });
    }
    render() {
        return (<div id="mapdiv"></div>);
    }
}

export default Map;