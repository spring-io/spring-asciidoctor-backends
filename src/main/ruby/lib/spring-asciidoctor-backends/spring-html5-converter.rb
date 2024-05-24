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

    # Remove <div class="details">...</div> if 'no-details' attribute is set
    if node.attr? 'no-details'
      html = html.gsub(/<div class="details">.*?<\/div>/m, "")
    end

    match = html.match(/^(.*<body.*?>)(.*)(<\/body>.*)$/m)
    templateFile = File.join(File.dirname(File.expand_path(__FILE__)), "body_template.html")
    body = File.read(templateFile, :encoding => 'UTF-8') % { :body => match[2] }
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

    # Modify the TOC to remove the period after top-level section numbers and wrap the number with a <span> tag
    if outline != nil && node.node_name == "document"
      outline.gsub!(/(<a href="#.+?>)(\d+)(\.)(\s+)/) do
        "#{$1}<span class=\"sectnum\">#{$2}</span>#{$4}" # Removed #{$3} which represents the period
      end
      outline.gsub!(/(<a href="#[^"]+">)((\d+\.)+)(\s+)/) do
        "#{$1}<span class=\"sectnum\">#{$2}</span>#{$4}"
      end
    end

    if node.node_name == "document" && node.attr("docname") != "index"
      index_href = node.attr "index-link", "index.html"
      swagger_href = node.attr "swagger-link"
      outline = %(<span id="back-to-index"><a href="#{index_href}">Back to index</a></span>) + outline
      if swagger_href != nil
        outline = outline + %(<span id="swagger"><a href="#{swagger_href}" target="_blank">Swagger (Open API Spec)</a></span>)
      end
    end
    return outline
  end

  def convert_inline_anchor(node)
    if inside_table?(node)
      # Use default processing for links inside tables
      super
    elsif node.type == :link
      attrs = node.id ? [%( id="#{node.id}")] : []
      if node.attr('window')
        attrs << %( class="external-link") # Add "external-link" only if 'window' is true
        target = %( target="_blank" rel="noopener")
      else
        target = ''
      end
      attrs << %( class="#{node.role}") if node.role
      title = node.attr('title') || "Visit #{node.target}" # Providing a default title if none is specified
      attrs << %( title="#{title}" data-tooltip-position="top" aria-label="#{node.target}")
      %(<a href="#{node.target}"#{attrs.join}#{target}>#{node.text}</a>)
    else
      super
    end
  end

  def inside_table?(node)
    current = node.parent
    while current
      return true if current.context == :table
      current = current.parent
    end
    false
  end

end
