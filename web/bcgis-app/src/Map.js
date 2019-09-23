import React from 'react';
import { Map, View } from 'ol';
import TileLayer from 'ol/layer/Tile';
import TileWms from 'ol/source/TileWMS';
import { fromLonLat } from 'ol/proj';
import { Map } from './MapClass';
export class Map extends React.Component {
    componentDidMount() {
        new Map({
            target: 'mapdiv',
            layers: [
                new TileLayer({
                    source: new TileWms({
                        url: 'http://localhost:8070/geoserver/D/wms',
                        params: {
                            'LAYERS': 'D:D',
                            'TILED': true
                        },
                        serverType: 'geoserver',
                        transition: 0
                    })
                })
            ],
            view: new View({
                center: fromLonLat([116.5, 40.18]),
                zoom: 9
            })
        });
    }
    render() {
        return (<div id="mapdiv"></div>);
    }
}
