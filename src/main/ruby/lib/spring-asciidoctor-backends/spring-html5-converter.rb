#
# Copyright 2021 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# coding: utf-8

require "asciidoctor/converter"
require "asciidoctor/converter/html5"
require "fileutils"

begin
  require "java"
rescue LoadError
end

class SpringHtml5Converter
  include Asciidoctor
  include Asciidoctor::Converter
  include Asciidoctor::Writer

  register_for "spring-html"

  def initialize(backend, opts = {})
    super
    @htmlConverter = DelegateHtml5Converter.new(backend, opts)
    syntax = opts[:htmlsyntax] == "xml" ? "xml" : "html"
    init_backend_traits basebackend: "html", filetype: "html", htmlsyntax: syntax, outfilesuffix: ".html", supports_templates: true
  end

  def convert(node, transform = node.node_name, opts = nil)
    if transform == "document"
      configure_node_attrs node
      return postprocess(node, @htmlConverter.convert(node, transform, opts))
    end
    if transform == "listing"
      return convert_listing node
    end
    return @htmlConverter.convert(node, transform, opts)
  end

  def write(output, target)
    File.open(target, "w") do |f|
      f.write(output)
    end
    write_assets File.dirname(target)
  end

  private

  def configure_node_attrs(node)
    node.set_attr "sectanchors"
    node.set_attr "linkcss"
    node.set_attr "stylesdir", "css"
    node.set_attr "stylesheet", "site.css"
    node.set_attr "icons", "font"
    node.remove_attr "iconfont-remote"
    node.remove_attr "copycss"
  end

  def postprocess(node, html)
    if node.attr? 'iconfont-fontawesome'
      html = html.gsub(/<link\ rel="stylesheet"(?!.*(site|font-awesome)\.css).*>\R?/, "")
    else
      html = html.gsub(/<link\ rel="stylesheet"(?!.*site\.css).*>\R?/, "")
    end
    match = html.match(/^(.*<body.*?>)(.*)(<\/body>.*)$/m)
    templateFile = File.join(File.dirname(File.expand_path(__FILE__)), "body_template.html")
    body = File.read(templateFile) % { :body => match[2] }
    return match[1] + body + match[3]
  end

  def write_assets(location)
    assets = File.join(File.dirname(File.expand_path(__FILE__)), "../../data/assets")
    FileUtils.cp_r assets + "/.", location if File.directory? assets
  end

  def convert_listing(node)
    nowrap = (node.option? "nowrap") || !(node.document.attr? "prewrap")
    if node.style == "source"
      lang = node.attr "language"
      pre_open = %(<pre class="highlight#{nowrap ? " nowrap" : ""}"><code#{lang ? %[ class="language-#{lang}" data-lang="#{lang}"] : ""}>)
      pre_close = "</code></pre>"
    else
      pre_open = %(<pre#{nowrap ? ' class="nowrap"' : ""}>)
      pre_close = "</pre>"
    end
    id_attribute = node.id ? %( id="#{node.id}") : ""
    title_element = node.title? ? %(<div class="title">#{node.captioned_title}</div>\n) : ""
    %(<div#{id_attribute} class="listingblock#{(role = node.role) ? " #{role}" : ""}">
#{title_element}<div class="content">
#{pre_open + (convert_listing_content(node) || "") + pre_close}
</div>
</div>)
  end

  def convert_listing_content(node)
    begin
      return Java::IoSpringAsciidoctorBackendCodetools::HtmlListingContentConverters.content node
    rescue NameError
      return node.content
    end
  end
end

class DelegateHtml5Converter < Asciidoctor::Converter::Html5Converter
  def convert_outline(node, opts = {})
    outline = super
    if node.node_name == "document" && node.attr("docname") != "index"
      index_href = node.attr "index-link", "index.html"
      outline = %(<span id="back-to-index"><a href="#{index_href}">Back to index</a></span>) + outline
    end
    return outline
  end
end
