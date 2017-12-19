siddhi-gpl-execution-geo
======================================

The **siddhi-gpl-execution-geo extension** is an extension to <a target="_blank" href="https://wso2.github.io/siddhi">Siddhi</a> that provides geo data related functionality such as checking whether a given geo coordinate is within a predefined geo-fence, etc. Following are the functions of the Geo extension.


Find some useful links below:

* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-geo">Source code</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-geo/releases">Releases</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-geo/issues">Issue tracker</a>

## Latest API Docs 

Latest API Docs is <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-geo/api/4.0.9">4.0.9</a>.

## How to use 

**Using the extension in <a target="_blank" href="https://github.com/wso2/product-sp">WSO2 Stream Processor</a>**

* You can use this extension in the latest <a target="_blank" href="https://github.com/wso2/product-sp/releases">WSO2 Stream Processor</a> that is a part of <a target="_blank" href="http://wso2.com/analytics?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">WSO2 Analytics</a> offering, with editor, debugger and simulation support. 

* To use this extension, you have to add the component <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-geo/releases">jar</a> in to the `<STREAM_PROCESSOR_HOME>/lib` directory.

**Using the extension as a <a target="_blank" href="https://wso2.github.io/siddhi/documentation/running-as-a-java-library">java library</a>**

* This extension can be added as a maven dependency along with other Siddhi dependencies to your project.

```
     <dependency>
        <groupId>org.wso2.extension.siddhi.gpl-execution-geo</groupId>
        <artifactId>siddhi-gpl-execution-geo</artifactId>
        <version>x.x.x</version>
     </dependency>
```

## Jenkins Build Status

---

|  Branch | Build Status |
| :------ |:------------ | 
| master  | [![Build Status](https://wso2.org/jenkins/view/All%20Builds/job/siddhi/job/siddhi-gpl-execution-geo/badge/icon)](https://wso2.org/jenkins/view/All%20Builds/job/siddhi/job/siddhi-gpl-execution-geo/) |

---

## Features

* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-geo/api/4.0.9/#distance-function">distance</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#function">(Function)</a>)*<br><div style="padding-left: 1em;"><p>This method gives the distance between two geo locations in meters</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-geo/api/4.0.9/#intersects-function">intersects</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#function">(Function)</a>)*<br><div style="padding-left: 1em;"><p>This function can be called using two sets of parameters. <br>&nbsp;First method will return true if the incoming event geo.json.geometry intersects the given geo.json.geometryFence else false.<br>&nbsp;Second method will return true if the location pointed by longitude and latitude intersects the given geo.json.geometryFence else false <br>&nbsp;Please refer examples</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-geo/api/4.0.9/#within-function">within</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#function">(Function)</a>)*<br><div style="padding-left: 1em;"><p>This function can be call using two sets of parameters.<br>&nbsp;This will returns true if the location specified in terms of longitude and latitude is within the geo.json.geometry.fence. <br>&nbsp;Or returns true if the geo.json.geometry is within the geo.json.geometry.fence.Returns false otherwise </p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-geo/api/4.0.9/#withindistance-function">withinDistance</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#function">(Function)</a>)*<br><div style="padding-left: 1em;"><p>This function can be call using two sets of parameters. <br>&nbsp;First method will returns true if the location specified in terms of longitude and latitude is within distance of the geo.json.geometry.fence. Returns false otherwise. <br>&nbsp;Second method will return true if the area given by geo.json.geometry is within distance of the geo.json.geometry.fence. <br>&nbsp;please refer examples </p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-geo/api/4.0.9/#closestpoints-stream-function">closestPoints</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-function">(Stream Function)</a>)*<br><div style="padding-left: 1em;"><p>This will return the closest geo point to the geo.json.geometry.fence</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-geo/api/4.0.9/#locationapproximate-stream-function">locationApproximate</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-function">(Stream Function)</a>)*<br><div style="padding-left: 1em;"><p>Geo Location Approximation compute the average location of the locationRecorder using the collection iBeacons which the location recorder resides.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-geo/api/4.0.9/#crosses-stream-processor">crosses</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processor">(Stream Processor)</a>)*<br><div style="padding-left: 1em;"><p>Returns true when the  the specified object of which the location is specified  in terms of longitude  and  latitude crosses the geographic location specified in geo.json.geometry.fence. Returns false when the object crosses out of the location specified in geo.json.geometry.fence. <br>&nbsp;Or Returns true when the object (i.e. geo.json.geometry) crosses the specified geographic location (i.e. geo.json.geometry.fence). Returns false when the object crosses out of geo.json.geometry.fence. </p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-geo/api/4.0.9/#proximity-stream-processor">proximity</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processor">(Stream Processor)</a>)*<br><div style="padding-left: 1em;"><p>This will returns true when two objects (specified in terms of longitude and latitude) are within the specified radius to another object. Returns false when the specified object moves out of the specified radius. The proximityWith optional attribute indicates the ID of the object that the object specified is in close proximity with. proximityID is a unique ID for the two objects in close proximity.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-geo/api/4.0.9/#stationary-stream-processor">stationary</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processor">(Stream Processor)</a>)*<br><div style="padding-left: 1em;"><p>Returns true when the object (defined in terms of  longitude  and latitude) becomes stationary within the specified radius. Returns false when the object moves out of the specified radius.</p></div>

## How to Contribute
 
  * Please report issues at <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-geo/issues">GitHub Issue Tracker</a>.
  
  * Send your contributions as pull requests to <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-geo/tree/master">master branch</a>. 
 
## Contact us 

 * Post your questions with the <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">"Siddhi"</a> tag in <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">Stackoverflow</a>. 
 
 * Siddhi developers can be contacted via the mailing lists:
 
    Developers List   : [dev@wso2.org](mailto:dev@wso2.org)
    
    Architecture List : [architecture@wso2.org](mailto:architecture@wso2.org)
 
## Support 

* We are committed to ensuring support for this extension in production. Our unique approach ensures that all support leverages our open development methodology and is provided by the very same engineers who build the technology. 

* For more details and to take advantage of this unique opportunity contact us via <a target="_blank" href="http://wso2.com/support?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">http://wso2.com/support/</a>. 
