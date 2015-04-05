<#macro feature code title>
<div class="bs-callout bs-callout-danger">
    <h4><a href="/features/details/${code}.html"><code>${title}</code></a></h4>

    <p>
        <#nested>
    </p>
</div>
</#macro>

<#macro comparison>
<div class="row row-comparison">
    <div class="col-lg-6 code-comparison">
        <h3>With Lombok</h3>

        <div class="snippet">@HTML_PRE@</div>
    </div>
    <div class="col-lg-6 code-comparison">
        <h3>Vanilla Java</h3>

        <div class="snippet">@HTML_POST@</div>
    </div>
</div>
</#macro>

<#macro page>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="google-site-verification" content="uCgX3Or3kDRGpbJ6JCsQc3Ub4JsnR5-p0itfsKAYZ_U"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="/favicon.ico" rel="icon" type="image/x-icon"/>

    <title>Project Lombok</title>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/modernizr/2.8.3/modernizr.min.js"></script>

    <link href="/css/bootstrap.css" rel="stylesheet">
    <link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css" rel="stylesheet">

    <link href="/css/custom.css" rel="stylesheet">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.2.0/js/bootstrap.min.js"></script>

    <script src="/js/swfobject.js"></script>
</head>
<body>
<a href="http://github.com/rzwitserloot/lombok" class="fork-me">
    <img
            alt="Fork me on GitHub"
            src="https://s3.amazonaws.com/github/ribbons/forkme_right_red_aa0000.png">
</a>

<div class="navbar navbar-default navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <a href="/index.html" class="navbar-brand">Lombok Project</a>
            <button class="navbar-toggle" type="button" data-toggle="collapse" data-target="#navbar-main">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
        </div>
        <div class="navbar-collapse collapse" id="navbar-main">
            <ul class="nav navbar-nav">
                <li class="dropdown">
                    <a class="dropdown-toggle pointer" data-toggle="dropdown">Features<span class="caret"></span></a>
                    <ul class="dropdown-menu" aria-labelledby="themes">
                        <li><a href="/features/stable.html">Stable</a></li>
                        <li><a href="/features/experimental.html">Experimental</a></li>
                        <li class="divider"></li>
                        <li><a href="/disable-checked-exceptions.html">Disable checked exceptions</a></li>
                    </ul>
                </li>
                <li>
                    <a href="http://groups.google.com/group/project-lombok" target="_blank">
                        <span>Discuss/Help</span>
                    </a>
                </li>
                <li>
                    <a href="http://wiki.github.com/rzwitserloot/lombok/contributing" target="_blank">
                        <span>Contribute</span>
                    </a>
                </li>
                <li class="dropdown">
                    <a class="dropdown-toggle pointer" data-toggle="dropdown">Install<span class="caret"></span></a>
                    <ul class="dropdown-menu" aria-labelledby="themes">
                        <li><a href="/install/compilers.html">Compilers</a></li>
                        <li class="divider"></li>
                        <li><a href="/install/ide.html">IDE</a></li>
                        <li class="divider"></li>
                        <li><a href="/install/android.html">Android</a></li>
                        <li><a href="/install/others.html">Frameworks & build systems</a></li>
                    </ul>
                </li>
                <li><a href="/download.html">Download</a></li>
            </ul>
        </div>
    </div>
</div>
<div class="container-fluid main-section">
    <#nested>
</div>
<footer class="container">
    <footer class="footer text-center">
        <div class="container">
            <a href="/credits.html">credits</a> | Copyright &copy; 2009-2015 The Project Lombok
            Authors, licensed under the <a href="http://www.opensource.org/licenses/mit-license.php">MIT
            license</a>.
        </div>
    </footer>
</footer>
</body>
</html>
</#macro>
